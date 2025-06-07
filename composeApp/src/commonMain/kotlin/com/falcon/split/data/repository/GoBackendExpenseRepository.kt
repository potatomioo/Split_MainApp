package com.falcon.split.data.repository

import com.falcon.split.data.network.KtorApiClient
import com.falcon.split.data.network.models_app.Expense
import com.falcon.split.data.network.models_app.ExpenseSplit
import com.falcon.split.data.network.models_app.ExpenseType
import com.falcon.split.data.network.models_app.Settlement
import com.falcon.split.data.network.models_app.SettlementStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AddExpenseRequest(
    @SerialName("groupId") val groupId: String,
    @SerialName("description") val description: String,
    @SerialName("amount") val amount: Double,
    @SerialName("expenseType") val expenseType: String
)

@Serializable
data class SettleBalanceRequest(
    @SerialName("groupId") val groupId: String,
    @SerialName("toUserId") val toUserId: String,
    @SerialName("amount") val amount: Double
)

@Serializable
data class ExpenseResponse(
    @SerialName("expenseId") val expenseId: String,
    @SerialName("groupId") val groupId: String,
    @SerialName("description") val description: String,
    @SerialName("amount") val amount: Double,
    @SerialName("expenseType") val expenseType: String,
    @SerialName("paidByUserId") val paidByUserId: String,
    @SerialName("paidByUserName") val paidByUserName: String,
    @SerialName("timestamp") val timestamp: Long,
    @SerialName("splits") val splits: List<SplitResponse>
)

@Serializable
data class SplitResponse(
    @SerialName("userId") val userId: String? = null,
    @SerialName("userName") val userName: String? = null,
    @SerialName("amount") val amount: Double
)

@Serializable
data class SettlementResponse(
    @SerialName("id") val id: String,
    @SerialName("groupId") val groupId: String,
    @SerialName("fromUserId") val fromUserId: String,
    @SerialName("fromUserName") val fromUserName: String,
    @SerialName("toUserId") val toUserId: String,
    @SerialName("toUserName") val toUserName: String,
    @SerialName("amount") val amount: Double,
    @SerialName("status") val status: String,
    @SerialName("timestamp") val timestamp: Long
)

class GoBackendExpenseRepository(private val ktorApiClient: KtorApiClient) : ExpenseRepository {

    override suspend fun addExpense(
        groupId: String,
        description: String,
        amount: Double,
        expenseType: ExpenseType
    ): Result<Unit> {
        return try {
            val request = AddExpenseRequest(
                groupId = groupId,
                description = description,
                amount = amount,
                expenseType = expenseType.name
            )
            ktorApiClient.post<ExpenseResponse>("api/expenses", request)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getExpensesByGroup(groupId: String): Flow<List<Expense>> = flow {
        try {
            val response: List<ExpenseResponse> = ktorApiClient.get("api/expenses/group/$groupId")
            val expenses = response.map { mapResponseToExpense(it) }
            emit(expenses)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun getExpensesByUser(userId: String): Flow<List<Expense>> = flow {
        try {
            val response: List<ExpenseResponse> = ktorApiClient.get("api/expenses/user")
            val expenses = response.map { mapResponseToExpense(it) }
            emit(expenses)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun settleBalance(
        groupId: String,
        fromUserId: String,
        toUserId: String,
        amount: Double
    ): Result<Unit> {
        return try {
            val request = SettleBalanceRequest(
                groupId = groupId,
                toUserId = toUserId,
                amount = amount
            )
            ktorApiClient.post<SettlementResponse>("api/settlements", request)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun approveSettlement(settlementId: String): Result<Unit> {
        return try {
            ktorApiClient.patch<SettlementResponse>("api/settlements/$settlementId/approve")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun declineSettlement(settlementId: String): Result<Unit> {
        return try {
            ktorApiClient.patch<SettlementResponse>("api/settlements/$settlementId/decline")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPendingSettlementsForUser(userId: String): Flow<List<Settlement>> =
        flow {
            try {
                val response: List<SettlementResponse> = ktorApiClient.get("api/settlements/pending")
                val settlements = response.map { mapResponseToSettlement(it) }
                emit(settlements)
            } catch (e: Exception) {
                emit(emptyList())
            }
        }

    override suspend fun getSettlementHistory(groupId: String): Flow<List<Settlement>> = flow {
        try {
            val response: List<SettlementResponse> = ktorApiClient.get("api/settlements/group/$groupId")
            val settlements = response.map { mapResponseToSettlement(it) }
            emit(settlements)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    private fun mapResponseToExpense(response: ExpenseResponse): Expense {
        val splits = response.splits.map { splitResponse ->
            ExpenseSplit(
                userId = splitResponse.userId ?: "",
                amount = splitResponse.amount,
                settled = false, // This would need to be determined by business logic
                phoneNumber = "" // Not provided in response, could be fetched separately if needed
            )
        }

        return Expense(
            expenseId = response.expenseId,
            groupId = response.groupId,
            description = response.description,
            amount = response.amount,
            type = response.expenseType,
            createdAt = response.timestamp,
            paidByUserId = response.paidByUserId,
            paidByUserName = response.paidByUserName,
            splits = splits
        )
    }

    private fun mapResponseToSettlement(response: SettlementResponse): Settlement {
        val status = when (response.status) {
            "PENDING" -> SettlementStatus.PENDING
            "APPROVED" -> SettlementStatus.APPROVED
            "DECLINED" -> SettlementStatus.DECLINED
            else -> SettlementStatus.PENDING
        }

        return Settlement(
            id = response.id,
            groupId = response.groupId,
            fromUserId = response.fromUserId,
            toUserId = response.toUserId,
            amount = response.amount,
            timestamp = response.timestamp,
            status = status,
            fromUserName = response.fromUserName,
            toUserName = response.toUserName
        )
    }
}
