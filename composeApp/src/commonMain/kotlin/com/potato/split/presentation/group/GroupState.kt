package com.potato.split.presentation.group

import com.potato.split.data.network.models_app.Group

sealed class GroupState {
    data object Loading : GroupState()
    data class Success(val groups: List<Group>) : GroupState()
    object Empty : GroupState()
    data class GroupDetailSuccess(val group: Group) : GroupState()
    data class Error(val message: String) : GroupState()
}