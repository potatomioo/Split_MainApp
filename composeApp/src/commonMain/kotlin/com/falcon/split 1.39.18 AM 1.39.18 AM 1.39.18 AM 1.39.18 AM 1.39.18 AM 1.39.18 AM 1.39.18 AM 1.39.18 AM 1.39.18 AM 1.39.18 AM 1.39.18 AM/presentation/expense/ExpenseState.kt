package com.falcon.split.presentation.expense

import com.falcon.split.data.network.models_app.Expense

sealed class ExpenseState {
    data object Loading : ExpenseState()
    data class Success(val expenses: List<Expense>) : ExpenseState()
    data class Error(val message: String) : ExpenseState()
}