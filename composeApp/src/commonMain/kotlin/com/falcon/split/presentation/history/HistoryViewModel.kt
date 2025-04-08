package com.falcon.split.presentation.screens.mainNavigation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.falcon.split.HistoryRepository
import com.falcon.split.presentation.history.HistoryActionType
import com.falcon.split.presentation.history.HistoryFilterType
import com.falcon.split.presentation.history.HistoryItem
import com.falcon.split.presentation.history.HistoryPagination
import com.falcon.split.presentation.history.HistoryState
import com.falcon.split.presentation.history.UserHistory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val historyRepository: HistoryRepository
) : ViewModel() {

    // State flows
    private val _historyState = MutableStateFlow<HistoryState>(HistoryState.Loading)
    val historyState = _historyState.asStateFlow()

    private val _historyItems = MutableStateFlow<List<HistoryItem>>(emptyList())
    val historyItems: StateFlow<List<HistoryItem>> = _historyItems.asStateFlow()

    private val _pagination = MutableStateFlow(HistoryPagination())
    val pagination: StateFlow<HistoryPagination> = _pagination.asStateFlow()

    private val _filterType = MutableStateFlow<HistoryFilterType>(HistoryFilterType.ALL)
    val filterType: StateFlow<HistoryFilterType> = _filterType.asStateFlow()

    // Search state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Refreshing state
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    // Unread count
    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    private val _recentHistoryItems = MutableStateFlow<List<HistoryItem>>(emptyList())
    val recentHistoryItems: StateFlow<List<HistoryItem>> = _recentHistoryItems.asStateFlow()


    init {
        loadInitialHistory()
    }
    fun loadInitialHistory() {
        _pagination.update { it.copy(currentPage = 0, hasMoreItems = true, isLoading = false) }
        _historyItems.value = emptyList()
        loadHistoryItems()
    }
    fun refreshHistory() {
        _isRefreshing.value = true
        loadInitialHistory()
    }
    fun loadMoreHistory() {
        val currentPagination = _pagination.value

        // Check if we can load more
        if (!currentPagination.hasMoreItems || currentPagination.isLoading) {
            println("DEBUG ViewModel: Cannot load more. HasMore: ${currentPagination.hasMoreItems}, IsLoading: ${currentPagination.isLoading}")
            return
        }

        println("DEBUG ViewModel: Loading page ${currentPagination.currentPage + 1}")

        // Update pagination state
        _pagination.update {
            it.copy(
                currentPage = it.currentPage + 1,
                isLoading = true
            )
        }

        // Load the next page
        loadHistoryItems()
    }
    fun setFilterType(type: HistoryFilterType) {
        if (_filterType.value != type) {
            _filterType.value = type
            loadInitialHistory()
        }
    }
    fun setSearchQuery(query: String) {
        if (_searchQuery.value != query) {
            _searchQuery.value = query
            applyFiltersAndSearch()
        }
    }
    fun markAsRead(historyItemId: String) {
        viewModelScope.launch {
            historyRepository.markHistoryItemAsRead(historyItemId)

            // Update the local list
            val updatedItems = _historyItems.value.map {
                if (it.id == historyItemId) it.copy(read = true) else it
            }
            _historyItems.value = updatedItems

            // Update the unread count
            updateUnreadCount()
        }
    }
    fun markAllAsRead() {
        viewModelScope.launch {
            historyRepository.markAllHistoryAsRead()

            // Update the local list
            val updatedItems = _historyItems.value.map { it.copy(read = true) }
            _historyItems.value = updatedItems

            // Update the unread count
            _unreadCount.value = 0
        }
    }
    private fun applyFiltersAndSearch() {
        viewModelScope.launch {
            _historyState.value = HistoryState.Loading

            try {
                loadHistoryItems()
            } catch (e: Exception) {
                _historyState.value = HistoryState.Error(e.message ?: "Unknown error")
            }
        }
    }
    private fun loadHistoryItems() {
        viewModelScope.launch {
            val page = _pagination.value.currentPage
            val itemsPerPage = _pagination.value.itemsPerPage

            try {
                // Set loading state
                _pagination.update { it.copy(isLoading = true) }

                // Check if we're refreshing or loading more
                val isRefreshing = _isRefreshing.value
                val isLoadingMore = page > 0

                // If we're refreshing, set state to Loading
                if (isRefreshing) {
                    _historyState.value = HistoryState.Loading
                }

                // Fetch the data
                historyRepository.getUserHistory(page, itemsPerPage)
                    .collect { newItems ->
                        // Apply filter if needed
                        val filteredItems = filterItems(newItems)

                        // If this is page 0 or refreshing, replace all items
                        // Otherwise, append to existing items
                        val combinedItems = if (page == 0 || isRefreshing) {
                            filteredItems
                        } else {
                            _historyItems.value + filteredItems
                        }

                        // Update the list
                        _historyItems.value = combinedItems

                        // Update state to success
                        _historyState.value = HistoryState.Success(combinedItems)

                        // Check if there are more items to load
                        val hasMore = historyRepository.hasMoreHistory(page, itemsPerPage)

                        // Update pagination state
                        _pagination.update {
                            it.copy(
                                hasMoreItems = hasMore,
                                isLoading = false
                            )
                        }

                        // Update unread count
                        updateUnreadCount()

                        // If refreshing, stop refreshing
                        if (_isRefreshing.value) {
                            _isRefreshing.value = false
                        }

                        // Log what happened
                        println("DEBUG ViewModel: Loaded ${filteredItems.size} items for page $page. " +
                                "Total items: ${combinedItems.size}. Has more: $hasMore")
                    }
            } catch (e: Exception) {
                println("DEBUG ViewModel: Error loading history - ${e.message}")
                _historyState.value = HistoryState.Error(e.message ?: "Unknown error")
                _pagination.update { it.copy(isLoading = false) }
                _isRefreshing.value = false
            }
        }
    }
    private fun filterItems(items: List<HistoryItem>): List<HistoryItem> {
        var result = items

        // Apply action type filter
        result = when (_filterType.value) {
            HistoryFilterType.ALL -> result
            HistoryFilterType.EXPENSES -> result.filter {
                it.actionType == HistoryActionType.EXPENSE_ADDED
            }
            HistoryFilterType.SETTLEMENTS -> result.filter {
                it.actionType == HistoryActionType.SETTLEMENT_REQUESTED ||
                        it.actionType == HistoryActionType.SETTLEMENT_APPROVED ||
                        it.actionType == HistoryActionType.SETTLEMENT_DECLINED
            }
        }

        // Apply search query if not empty
        if (_searchQuery.value.isNotEmpty()) {
            val searchQueryLower = _searchQuery.value.lowercase()
            result = result.filter {
                it.description.lowercase().contains(searchQueryLower) ||
                        (it.groupName?.lowercase()?.contains(searchQueryLower) == true) ||
                        (it.actionByUserName?.lowercase()?.contains(searchQueryLower) == true) ||
                        (it.targetUserName?.lowercase()?.contains(searchQueryLower) == true)
            }
        }

        return result
    }
    private fun updateUnreadCount() {
        val count = _historyItems.value.count { !it.read }
        _unreadCount.value = count
    }
    fun loadRecentHistory(limit: Int = 4) {
        viewModelScope.launch {
            try {
                historyRepository.getRecentHistory(limit)
                    .collect { items ->
                        _recentHistoryItems.value = items
                        println("DEBUG: Loaded ${items.size} recent history items")
                    }
            } catch (e: Exception) {
                println("DEBUG: Error loading recent history - ${e.message}")
            }
        }
    }
}