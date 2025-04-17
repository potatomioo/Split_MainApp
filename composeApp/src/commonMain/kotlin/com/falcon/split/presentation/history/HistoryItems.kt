package com.falcon.split.presentation.history

import com.falcon.split.data.network.models_app.GroupType
import kotlinx.datetime.Clock
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

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
    val id: String = "",
    val timestamp: Long = Clock.System.now().toEpochMilliseconds(),
    val actionType: HistoryActionType,
    val actionByUserId: String,
    val actionByUserName: String? = null,
    val groupId: String? = null,
    val groupName: String? = null,
    val groupType: String? = null,
    val expenseId: String? = null,
    val expenseAmount: Double? = null,
    val expenseType: String? = null,
    val settlementId: String? = null,
    val settlementAmount: Double? = null,
    val targetUserId: String? = null,
    val targetUserName: String? = null,
    val description: String = "",
    val read: Boolean = false
)

// In common main
@Serializable
data class UserHistory(
    val userId: String = "",
    val historyItems: List<HistoryItem> = emptyList(),
    @SerialName("lastUpdated")
    private val _lastUpdated: Long? = null
) {
    @Transient
    val lastUpdated: Long = _lastUpdated ?: Clock.System.now().toEpochMilliseconds()
}

@Serializable
enum class HistoryFilterType {
    ALL,
    EXPENSES,
    SETTLEMENTS
}