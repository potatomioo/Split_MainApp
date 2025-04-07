package com.falcon.split.presentation.expense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.falcon.split.data.Repository.ExpenseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ExpenseViewModel(
    private val expenseRepository: ExpenseRepository
) : ViewModel() {
    private val _expenseState = MutableStateFlow<ExpenseState>(ExpenseState.Loading)
    val expenseState = _expenseState.asStateFlow()

    fun loadGroupExpenses(groupId: String) {
        viewModelScope.launch {
            try {
                expenseRepository.getExpensesByGroup(groupId)
                    .collect { expenses ->
                        _expenseState.value = ExpenseState.Success(expenses)
                    }
            } catch (e: Exception) {
                _expenseState.value = ExpenseState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun loadUserExpenses(userId: String) {
        viewModelScope.launch {
            try {
                expenseRepository.getExpensesByUser(userId)
                    .collect { expenses ->
                        _expenseState.value = ExpenseState.Success(expenses)
                    }
            } catch (e: Exception) {
                _expenseState.value = ExpenseState.Error(e.message ?: "Unknown error")
            }
        }
    }

//    fun addExpense(groupId: String, expense: Expense) {
//        viewModelScope.launch {
//            try {
//                expenseRepository.addExpense(groupId, expense)
//                    .onFailure { error ->
//                        _expenseState.value = ExpenseState.Error(error.message ?: "Failed to add expense")
//                    }
//            } catch (e: Exception) {
//                _expenseState.value = ExpenseState.Error(e.message ?: "Unknown error")
//            }
//        }
//    }
}