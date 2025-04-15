package com.falcon.split.data.network.models_app

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.DrawableResource
import split.composeapp.generated.resources.Group_Entertainment
import split.composeapp.generated.resources.Group_Home
import split.composeapp.generated.resources.Group_Office
import split.composeapp.generated.resources.Group_Others
import split.composeapp.generated.resources.Group_Sports
import split.composeapp.generated.resources.Group_trip
import split.composeapp.generated.resources.Res
import split.composeapp.generated.resources.expense_bills
import split.composeapp.generated.resources.expense_entertainment
import split.composeapp.generated.resources.expense_food
import split.composeapp.generated.resources.expense_groceries
import split.composeapp.generated.resources.expense_others
import split.composeapp.generated.resources.expense_shopping
import split.composeapp.generated.resources.expense_stay
import split.composeapp.generated.resources.expense_travel

@Serializable
data class Expense(
    val expenseId: String = "",
    val groupId: String = "",
    val description: String = "",
    val amount: Double = 0.0,
    val type: String = ExpenseType.OTHER.name,
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

enum class ExpenseType(val displayName: String, val iconRes: DrawableResource) {
    TRAVEL("Travel", Res.drawable.expense_travel),
    STAY("Stay", Res.drawable.expense_stay),
    SHOPPING("Shopping", Res.drawable.expense_shopping),
    GROCERIES("Groceries", Res.drawable.expense_groceries),
    FOOD("Food", Res.drawable.expense_food),
    BILLS("Bills", Res.drawable.expense_bills),
    ENTERTAINMENT("Entertainment", Res.drawable.expense_entertainment),
    OTHER("Others", Res.drawable.expense_others);

    companion object {
        fun fromString(value: String?): ExpenseType {
            return values().find { it.name == value } ?: OTHER
        }
    }
}