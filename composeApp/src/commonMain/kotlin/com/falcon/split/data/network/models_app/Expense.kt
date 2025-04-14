package com.falcon.split.data.network.models_app

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Expense(
    val expenseId: String = "",
    val groupId: String = "",
    val description: String = "",
    val amount: Double = 0.0,
    val createdAt : Long = Clock.System.now().toEpochMilliseconds(),
    val paidByUserId: String = "",
    val paidByUserName: String? = "",
    val splits: List<ExpenseSplit> = emptyList(),
)

@Serializable
data class ExpenseSplit(
    val userId: String = "",
    val amount: Double = 0.0,
    val settled: Boolean = false,
    val phoneNumber: String = ""
)