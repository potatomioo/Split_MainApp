package com.falcon.split.Presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.falcon.split.data.Repository.HistoryRepository
import com.falcon.split.data.network.models_app.Transaction
import com.falcon.split.data.network.models_app.TransactionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime

class HistoryViewModel(
    private val historyRepository: HistoryRepository
) : ViewModel() {

    private val _state = MutableStateFlow<HistoryState>(HistoryState.Loading)
    val state = _state.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedFilter = MutableStateFlow(TransactionFilter.ALL)
    val selectedFilter = _selectedFilter.asStateFlow()

    private val _showFilterOptions = MutableStateFlow(false)
    val showFilterOptions = _showFilterOptions.asStateFlow()

    private var currentPage = 0
    private val pageSize = 20
    private var hasMoreData = true

    init {
        loadTransactions()
    }

    fun loadTransactions(refresh: Boolean = false) {
        if (refresh) {
            currentPage = 0
            hasMoreData = true
        }

        if (!hasMoreData && !refresh) return

        viewModelScope.launch {
            try {
                val fromTimestamp = if (currentPage == 0) null else {
                    // For pagination, use the timestamp of the last loaded transaction
                    val currentTransactions = (_state.value as? HistoryState.Success)?.transactions ?: emptyList()
                    currentTransactions.lastOrNull()?.timestamp
                }

                historyRepository.getUserTransactionHistory(
                    limit = pageSize,
                    offset = currentPage * pageSize,
                    fromTimestamp = fromTimestamp
                ).collect { transactions ->
                    // Check if we have more data to load
                    hasMoreData = transactions.size >= pageSize

                    // Get existing transactions if we're paginating
                    val existingTransactions = if (currentPage > 0 && _state.value is HistoryState.Success) {
                        (_state.value as HistoryState.Success).transactions
                    } else {
                        emptyList()
                    }

                    // Apply filters based on the current filter setting
                    val filteredTransactions = when(_selectedFilter.value) {
                        TransactionFilter.ALL -> transactions
                        TransactionFilter.EXPENSES -> transactions.filter { it.type == TransactionType.EXPENSE }
                        TransactionFilter.SETTLEMENTS -> transactions.filter { it.type == TransactionType.SETTLEMENT }
                    }

                    // Combine with existing transactions if paginating
                    val combinedTransactions = if (refresh) {
                        filteredTransactions
                    } else {
                        existingTransactions + filteredTransactions
                    }

                    // Group transactions by time period
                    val groupedTransactions = groupTransactionsByTimePeriod(combinedTransactions)

                    _state.value = HistoryState.Success(
                        transactions = combinedTransactions,
                        groupedTransactions = groupedTransactions
                    )

                    // Increment page for next load
                    if (!refresh) {
                        currentPage++
                    }
                }
            } catch (e: Exception) {
                _state.value = HistoryState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun loadMoreTransactions() {
        loadTransactions(false)
    }

    fun refreshTransactions() {
        loadTransactions(true)
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query

        if (query.isBlank()) {
            // If search query is empty, reset to normal transaction list
            loadTransactions(true)
            return
        }

        viewModelScope.launch {
            _state.value = HistoryState.Loading

            try {
                historyRepository.searchTransactions(query).collect { transactions ->
                    // Group transactions by time period
                    val groupedTransactions = groupTransactionsByTimePeriod(transactions)

                    _state.value = HistoryState.Success(
                        transactions = transactions,
                        groupedTransactions = groupedTransactions,
                        isSearchResult = true
                    )
                }
            } catch (e: Exception) {
                _state.value = HistoryState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun setFilter(filter: TransactionFilter) {
        if (_selectedFilter.value == filter) return

        _selectedFilter.value = filter
        loadTransactions(true)
    }

    fun toggleFilterOptions() {
        _showFilterOptions.value = !_showFilterOptions.value
    }

    fun clearSearch() {
        _searchQuery.value = ""
        loadTransactions(true)
    }

    private fun groupTransactionsByTimePeriod(transactions: List<Transaction>): Map<String, List<Transaction>> {
        val now = Clock.System.now()
        val today = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
        val yesterday = today.minus(kotlinx.datetime.DatePeriod(days = 1))

        // Group by time period
        val grouped = transactions.groupBy { transaction ->
            val instant = Instant.fromEpochMilliseconds(transaction.timestamp)
            val transactionDate = instant.toLocalDateTime(TimeZone.currentSystemDefault()).date

            when {
                transactionDate == today -> "Today"
                transactionDate == yesterday -> "Yesterday"
                today.minus(kotlinx.datetime.DatePeriod(days = 7)) < transactionDate -> "This Week"
                today.minus(kotlinx.datetime.DatePeriod(days = 30)) < transactionDate -> "This Month"
                today.minus(kotlinx.datetime.DatePeriod(days = 365)) < transactionDate -> "This Year"
                else -> "Earlier"
            }
        }

        // Sort the groups by custom order using a LinkedHashMap
        val sortedGroups = mutableListOf<Pair<String, List<Transaction>>>()

        // Add groups in desired order
        listOf("Today", "Yesterday", "This Week", "This Month", "This Year", "Earlier").forEach { key ->
            grouped[key]?.let { group ->
                sortedGroups.add(Pair(key, group))
            }
        }

        return sortedGroups.toMap()
    }
}

sealed class HistoryState {
    object Loading : HistoryState()
    data class Success(
        val transactions: List<Transaction>,
        val groupedTransactions: Map<String, List<Transaction>>,
        val isSearchResult: Boolean = false
    ) : HistoryState()
    data class Error(val message: String) : HistoryState()
}

enum class TransactionFilter {
    ALL,
    EXPENSES,
    SETTLEMENTS
}