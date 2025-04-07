package com.falcon.split.presentation.history

import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable

@Serializable
enum class HistoryActionType {
    GROUP_CREATED,   // Someone created a group and added the user
    GROUP_DELETED,   // A group was deleted that the user was part of
    EXPENSE_ADDED,   // An expense was added to a group
    SETTLEMENT_REQUESTED, // A settlement was requested
    SETTLEMENT_APPROVED,  // A settlement was approved/completed
    SETTLEMENT_DECLINED,  // A settlement was declined
    MEMBER_ADDED     // A new member was added to a group
}

@Serializable
data class HistoryItem(
    val id: String = "",  // Unique ID for the history item (will be auto-generated)
    val timestamp: Long = Clock.System.now().toEpochMilliseconds(),
    val actionType: HistoryActionType,
    val actionByUserId: String, // User ID who performed the action
    val actionByUserName: String? = null, // Name of the user who performed the action
    val groupId: String? = null, // Related group ID (if applicable)
    val groupName: String? = null, // Group name (if applicable)
    val expenseId: String? = null, // Related expense ID (if applicable)
    val expenseAmount: Double? = null, // Expense amount (if applicable)
    val settlementId: String? = null, // Related settlement ID (if applicable)
    val settlementAmount: Double? = null, // Settlement amount (if applicable)
    val targetUserId: String? = null, // Target user ID (for settlements, member additions)
    val targetUserName: String? = null, // Name of the target user
    val description: String = "", // Description of the action
    val read: Boolean = false // Whether the user has read this history item
)

@Serializable
data class UserHistory(
    val userId: String = "",  // User ID this history belongs to
    val historyItems: List<HistoryItem> = emptyList(),
    val lastUpdated: Long = Clock.System.now().toEpochMilliseconds()
)

@Serializable
enum class HistoryFilterType {
    ALL,
    EXPENSES,
    SETTLEMENTS
}