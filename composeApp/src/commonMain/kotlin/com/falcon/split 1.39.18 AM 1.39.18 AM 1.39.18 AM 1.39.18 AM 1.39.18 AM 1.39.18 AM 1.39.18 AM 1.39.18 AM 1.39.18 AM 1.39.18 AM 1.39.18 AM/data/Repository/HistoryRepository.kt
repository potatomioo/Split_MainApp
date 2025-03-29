package com.falcon.split.data.Repository

import com.falcon.split.data.network.models_app.Transaction
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for accessing transaction history data
 * This is in the common module and will be implemented platform-specifically
 */
interface HistoryRepository {
    /**
     * Get all transactions (expenses and settlements) for the current user
     * @param limit Number of records to fetch
     * @param offset Offset for pagination
     * @param fromTimestamp Optional timestamp to fetch transactions from a specific time
     */
    suspend fun getUserTransactionHistory(
        limit: Int = 20,
        offset: Int = 0,
        fromTimestamp: Long? = null
    ): Flow<List<Transaction>>

    /**
     * Search transactions by query string across descriptions, group names, and user names
     */
    suspend fun searchTransactions(query: String): Flow<List<Transaction>>

    /**
     * Filter transactions by various criteria
     */
    suspend fun filterTransactions(
        groupIds: List<String>? = null,
        transactionTypes: List<String>? = null,
        startDate: Long? = null,
        endDate: Long? = null,
        minAmount: Double? = null,
        maxAmount: Double? = null
    ): Flow<List<Transaction>>
}