package com.falcon.split.presentation.history



sealed class HistoryState {
    data object Loading : HistoryState()
    data class Success(val historyItems: List<HistoryItem>) : HistoryState()
    data class Error(val message: String) : HistoryState()
}

//For pagination
data class HistoryPagination(
    val currentPage: Int = 0,
    val itemsPerPage: Int = 10,
    val hasMoreItems: Boolean = true,
    val isLoading: Boolean = false
)
