package com.falcon.split.data.network.models_app

import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable

/**
 * Unified Transaction model for displaying both expenses and settlements in the History screen
 */
@Serializable
data class Transaction(
    val id: String = "",
    val groupId: String = "",
    val groupName: String = "",
    val description: String = "",
    val amount: Double = 0.0,
    val type: TransactionType = TransactionType.EXPENSE,
    val timestamp: Long = Clock.System.now().toEpochMilliseconds(),
    val paidByUserId: String = "",
    val paidByUserName: String? = "",
    val involvedUserIds: List<String> = emptyList(),
    val status: TransactionStatus = TransactionStatus.COMPLETED
)

enum class TransactionType {
    EXPENSE,     // A regular expense added to a group
    SETTLEMENT,  // A payment to settle a debt
    ADJUSTMENT   // A manual adjustment made to balances
}

enum class TransactionStatus {
    PENDING,    // For settlements that are awaiting approval
    COMPLETED,  // Finalized transactions
    CANCELLED   // Cancelled or declined transactions
}

/**
 * Extension function to convert an Expense to a Transaction
 */
fun Expense.toTransaction(groupName: String): Transaction {
    return Transaction(
        id = expenseId,
        groupId = groupId,
        groupName = groupName,
        description = description,
        amount = amount,
        type = TransactionType.EXPENSE,
        timestamp = expenseId.toLongOrNull() ?: Clock.System.now().toEpochMilliseconds(),
        paidByUserId = paidByUserId,
        paidByUserName = paidByUserName,
        involvedUserIds = splits?.map { it.userId } ?: emptyList(),
        status = TransactionStatus.COMPLETED
    )
}

/**
 * Extension function to convert a Settlement to a Transaction
 */
fun Settlement.toTransaction(groupName: String): Transaction {
    return Transaction(
        id = id,
        groupId = groupId,
        groupName = groupName,
        description = "Settlement",
        amount = amount,
        type = TransactionType.SETTLEMENT,
        timestamp = timestamp,
        paidByUserId = fromUserId,
        paidByUserName = fromUserName,
        involvedUserIds = listOf(fromUserId, toUserId),
        status = when(status) {
            SettlementStatus.PENDING -> TransactionStatus.PENDING
            SettlementStatus.APPROVED -> TransactionStatus.COMPLETED
            SettlementStatus.DECLINED -> TransactionStatus.CANCELLED
        }
    )
}