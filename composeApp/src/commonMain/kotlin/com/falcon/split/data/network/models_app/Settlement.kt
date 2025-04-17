package com.falcon.split.data.network.models_app

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Settlement(
    val id: String = "",
    val groupId: String = "",
    val fromUserId: String = "",  // The person who pays
    val toUserId: String = "",    // The person who receives
    val amount: Double = 0.0,
    val timestamp: Long = kotlinx.datetime.Clock.System.now().toEpochMilliseconds(),
    val status: SettlementStatus = SettlementStatus.PENDING,
    val fromUserName: String? = "",
    val toUserName: String? = ""
)

enum class SettlementStatus {
    PENDING,
    APPROVED,
    DECLINED
}

sealed class SettlementState {
    object Initial : SettlementState()
    object Loading : SettlementState()
    object Success : SettlementState()
    data class Error(val message: String) : SettlementState()
}

