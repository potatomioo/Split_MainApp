package com.falcon.split.presentation.expense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.falcon.split.data.Repository.ExpenseRepository
import com.falcon.split.data.Repository.GroupRepository
import com.falcon.split.data.network.models_app.Group
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class CreateExpenseViewModel(
    private val groupRepository: GroupRepository,
    private val expenseRepository: ExpenseRepository
) : ViewModel() {
    private val _state = MutableStateFlow<CreateExpenseState>(CreateExpenseState.Loading)
    val state = _state.asStateFlow()

    // For selected group details
    private val _selectedGroup = MutableStateFlow<Group?>(null)
    val selectedGroup = _selectedGroup.asStateFlow()

    init {
        loadGroups()
    }

    private fun loadGroups() {
        viewModelScope.launch {
            try {
                groupRepository.getCurrentUserGroups()
                    .collect { groups ->
                        _state.value = CreateExpenseState.Success(groups = groups)
                    }
            } catch (e: Exception) {
                _state.value = CreateExpenseState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun selectGroup(groupId: String) {
        viewModelScope.launch {
            try {
                groupRepository.getGroupDetails(groupId)
                    .collect { group ->
                        _selectedGroup.value = group
                    }
            } catch (e: Exception) {
                _state.value = CreateExpenseState.Error(e.message ?: "Failed to load group details")
            }
        }
    }

    fun createExpense(
        description: String,
        amount: Double,
        selectedGroupId: String
    ) {
        viewModelScope.launch {
            try {

                expenseRepository.addExpense(
                    groupId = selectedGroupId,
                    description = description,
                    amount = amount
                ).onSuccess {
                    // Navigate back or show success message
                }.onFailure { error ->
                    _state.value = CreateExpenseState.Error(error.message ?: "Failed to create expense")
                }
            } catch (e: Exception) {
                _state.value = CreateExpenseState.Error(e.message ?: "Unknown error")
            }
        }
    }
}

sealed class CreateExpenseState {
    object Loading : CreateExpenseState()
    data class Success(
        val selectedGroup: Group? = null,
        val groups: List<Group> = emptyList(),
    ) : CreateExpenseState()
    data class Error(val message: String) : CreateExpenseState()
}