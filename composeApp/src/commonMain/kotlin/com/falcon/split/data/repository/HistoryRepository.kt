package com.falcon.split

import com.falcon.split.presentation.history.HistoryItem
import kotlinx.coroutines.flow.Flow

interface HistoryRepository {
    suspend fun getUserHistory(page: Int, itemsPerPage: Int): Flow<List<HistoryItem>>
    suspend fun hasMoreHistory(page: Int, itemsPerPage: Int): Boolean
    suspend fun markHistoryItemAsRead(historyId: String): Result<Unit>
    suspend fun addHistoryItem(historyItem: HistoryItem): Result<Unit>
    suspend fun markAllHistoryAsRead(): Result<Unit>
    suspend fun getRecentHistory(limit: Int = 4): Flow<List<HistoryItem>>
}