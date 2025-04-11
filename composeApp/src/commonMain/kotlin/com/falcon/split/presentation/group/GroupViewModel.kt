package com.falcon.split.presentation.group

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.falcon.split.data.Repository.ExpenseRepository
import com.falcon.split.data.Repository.GroupRepository
import com.falcon.split.data.network.models_app.Settlement
import com.falcon.split.data.network.models_app.SettlementState
import com.falcon.split.presentation.expense.ExpenseState
import com.falcon.split.userManager.UserManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GroupViewModel(
    private val groupRepository: GroupRepository,
    private val expenseRepository: ExpenseRepository,
    userManager: UserManager
) : ViewModel() {
    private val _groupState = MutableStateFlow<GroupState>(GroupState.Loading)
    val groupState = _groupState.asStateFlow()

    private val _expenseState = MutableStateFlow<ExpenseState>(ExpenseState.Loading)
    val expenseState = _expenseState.asStateFlow()

    private val _settlementState = MutableStateFlow<SettlementState>(SettlementState.Initial)
    val settlementState = _settlementState.asStateFlow()

    private val _settlements = MutableStateFlow<List<Settlement>>(emptyList())
    val settlements = _settlements.asStateFlow()

    val currentUserId = userManager.getCurrentUserId()

    private val _pendingSettlements = MutableStateFlow<List<Settlement>>(emptyList())
    val pendingSettlements = _pendingSettlements.asStateFlow()

    private val _processingSettlementId = MutableStateFlow<Set<String>>(emptySet())
    val processingSettlementId = _processingSettlementId.asStateFlow()

    private val _deleteErrorEvent = MutableSharedFlow<String>()
    val deleteErrorEvent = _deleteErrorEvent.asSharedFlow()

    private val _deleteSuccess = MutableStateFlow(false)
    val deleteSuccess = _deleteSuccess.asStateFlow()


    init {
        loadGroups()
    }

    private fun addProcessingSettlement(settlementId: String) {
        _processingSettlementId.update { it + settlementId }
    }

    private fun removeProcessingSettlement(settlementId: String) {
        _processingSettlementId.update { it - settlementId }
    }


    fun loadGroups() {
        viewModelScope.launch {
            try {
                println("ViewModel: Starting to load groups...")
                _groupState.value = GroupState.Loading

                groupRepository.getCurrentUserGroups()
                    .collect { groups ->
                        println("ViewModel: Collected groups, size: ${groups.size}")
                        groups.forEach { group ->
                            println("ViewModel: Group details - Name: ${group.name}, CreatedBy: ${group.createdBy}")
                        }

                        _groupState.value = if (groups.isEmpty()) {
                            println("ViewModel: No groups found")
                            GroupState.Empty
                        } else {
                            println("ViewModel: Found ${groups.size} groups")
                            GroupState.Success(groups)
                        }
                    }
            } catch (e: Exception) {
                println("ViewModel Error: ${e.message}")
                e.printStackTrace() // Print full stack trace
                _groupState.value = GroupState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun addMembersToGroup(groupId: String, newMembers: List<String>) {
        viewModelScope.launch {
            try {
                groupRepository.addMembersToGroup(groupId, newMembers)
                    .onFailure { error ->
                        _groupState.value = GroupState.Error(error.message ?: "Failed to add members")
                    }
            } catch (e: Exception) {
                _groupState.value = GroupState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun loadGroupDetails(groupId: String) {
        viewModelScope.launch {
            try {
                groupRepository.getGroupDetails(groupId)
                    .collect { group ->
                        group?.let {
                            _groupState.value = GroupState.GroupDetailSuccess(it)
                        }
                    }
            } catch (e: Exception) {
                _groupState.value = GroupState.Error(e.message ?: "Unknown error")
            }
        }
    }

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

    fun loadSettlementHistory(groupId: String) {
        viewModelScope.launch {
            try {
                expenseRepository.getSettlementHistory(groupId)
                    .collect { settlementList ->
                        _settlements.value = settlementList
                    }
            } catch (e: Exception) {
                // Handle error if needed
            }
        }
    }

    fun settleBalance(groupId: String, toUserId: String, amount: Double) {
        viewModelScope.launch {
            _settlementState.value = SettlementState.Loading

            try {
                expenseRepository.settleBalance(
                    groupId = groupId,
                    fromUserId = currentUserId ?: "",
                    toUserId = toUserId,
                    amount = amount
                ).onSuccess {
                    _settlementState.value = SettlementState.Success

                    // Reload data immediately after settlement is created
                    loadGroupDetails(groupId)  // Reload full group data
                    loadSettlementHistory(groupId)
                    loadPendingSettlements()
                }.onFailure { error ->
                    _settlementState.value = SettlementState.Error(error.message ?: "Failed to settle")
                }
            } catch (e: Exception) {
                _settlementState.value = SettlementState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun loadPendingSettlements() {
        viewModelScope.launch {
            try {
                expenseRepository.getPendingSettlementsForUser(currentUserId?:"")
                    .collect { settlements ->
                        _pendingSettlements.value = settlements
                    }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun approveSettlement(settlementId: String) {
        viewModelScope.launch {

            if (_processingSettlementId.value.contains(settlementId)) return@launch
            addProcessingSettlement(settlementId)
            _settlementState.value = SettlementState.Loading

            try {
                expenseRepository.approveSettlement(settlementId)
                    .onSuccess {
                        _settlementState.value = SettlementState.Success
                    }
                    .onFailure { error ->
                        _settlementState.value = SettlementState.Error(error.message ?: "Failed to approve settlement")
                    }
            } catch (e: Exception) {
                _settlementState.value = SettlementState.Error(e.message ?: "Unknown error")
            }finally {
                removeProcessingSettlement(settlementId)
            }
        }
    }

    fun declineSettlement(settlementId: String) {
        viewModelScope.launch {
            if (_processingSettlementId.value.contains(settlementId)) return@launch

            addProcessingSettlement(settlementId)
            _settlementState.value = SettlementState.Loading

            try {
                expenseRepository.declineSettlement(settlementId)
                    .onSuccess {
                        _settlementState.value = SettlementState.Success
                    }
                    .onFailure { error ->
                        _settlementState.value = SettlementState.Error(error.message ?: "Failed to decline settlement")
                    }
            } catch (e: Exception) {
                _settlementState.value = SettlementState.Error(e.message ?: "Unknown error")
            }finally {
                removeProcessingSettlement(settlementId)
            }
        }
    }

    fun deleteGroup(groupId: String) {
        viewModelScope.launch {
            try {
                groupRepository.deleteGroup(groupId)
                    .onSuccess {
                        _deleteSuccess.value = true
                        loadGroups()
                    }
                    .onFailure { error ->
                        // Signal failure, with error message
                        _deleteErrorEvent.emit(error.message ?: "Failed to delete group")
                    }
            } catch (e: Exception) {
                _deleteErrorEvent.emit(e.message ?: "Unknown error")
            }
        }
    }

    fun resetSettlementState() {
        _settlementState.value = SettlementState.Initial
    }

    fun retryLoading() {
        loadGroups()
    }
}
