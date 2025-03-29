package com.falcon.split.data.Repository

import com.falcon.split.data.network.models_app.Expense
import com.falcon.split.data.network.models_app.Settlement
import kotlinx.coroutines.flow.Flow

interface ExpenseRepository {
    suspend fun addExpense(groupId: String, description: String, amount: Double): Result<Unit>
    suspend fun getExpensesByGroup(groupId: String): Flow<List<Expense>>
    suspend fun getExpensesByUser(userId: String): Flow<List<Expense>>

    //for settlement
    suspend fun settleBalance(groupId: String, fromUserId: String, toUserId: String, amount: Double): Result<Unit>
    suspend fun getSettlementHistory(groupId: String): Flow<List<Settlement>>

    suspend fun approveSettlement(settlementId: String): Result<Unit>
    suspend fun declineSettlement(settlementId: String): Result<Unit>

    // Get pending settlements for a user
    suspend fun getPendingSettlementsForUser(userId: String): Flow<List<Settlement>>
}