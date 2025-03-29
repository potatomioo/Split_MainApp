package com.falcon.split.data.network.models_app

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Expense(
    val expenseId: String = "",
    val groupId: String = "",  // Which group this expense belongs to
    val description: String = "",
    val amount: Double = 0.0,
    val paidByUserId: String = "",  // userId of person who paid
    val paidByUserName: String? = "",  // userName of person who paid
    val splits: List<ExpenseSplit> = emptyList(),
)

@Serializable
data class ExpenseSplit(
    val userId: String = "",
    val amount: Double = 0.0,
    val settled: Boolean = false,
    val phoneNumber: String = ""
)