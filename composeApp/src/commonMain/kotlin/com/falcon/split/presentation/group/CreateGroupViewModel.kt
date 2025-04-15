package com.falcon.split.presentation.group

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.falcon.split.contact.Contact
import com.falcon.split.data.Repository.GroupRepository
import com.falcon.split.data.network.models_app.GroupType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class CreateGroupState {
    data object Initial : CreateGroupState()
    data object Loading : CreateGroupState()
    data class Success(val groupId: String) : CreateGroupState()
    data class Error(val message: String) : CreateGroupState()
}

class CreateGroupViewModel(
    private val groupRepository: GroupRepository
) : ViewModel() {
    private val _state = MutableStateFlow<CreateGroupState>(CreateGroupState.Initial)
    val state = _state.asStateFlow()

    private val _selectedContacts = MutableStateFlow<List<Contact>>(emptyList())
    val selectedContacts = _selectedContacts.asStateFlow()

    private val _selectedGroupType = MutableStateFlow<GroupType>(GroupType.OTHER)
    val selectedGroupType = _selectedGroupType.asStateFlow()

    fun setGroupType(groupType: GroupType) {
        _selectedGroupType.value = groupType
    }

    fun addContact(contact: Contact) {
        val currentList = _selectedContacts.value.toMutableList()
        if (!currentList.any { it.contactNumber == contact.contactNumber }) {
            currentList.add(contact)
            _selectedContacts.value = currentList
        }
    }

    fun removeContact(contact: Contact) {
        val currentList = _selectedContacts.value.toMutableList()
        currentList.removeAll { it.contactNumber == contact.contactNumber }
        _selectedContacts.value = currentList
    }

    fun createGroup(name: String) {
        viewModelScope.launch {
            _state.value = CreateGroupState.Loading

            try {
                groupRepository.createGroup(name, _selectedContacts.value, _selectedGroupType.value)
                    .onSuccess { group ->
                        _state.value = CreateGroupState.Success(group.id)
                    }
                    .onFailure { error ->
                        _state.value = CreateGroupState.Error(error.message ?: "Failed to create group")
                    }
            } catch (e: Exception) {
                _state.value = CreateGroupState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }
}