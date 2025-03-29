package com.falcon.split.presentation.group

import com.falcon.split.data.network.models_app.Group

sealed class GroupState {
    data object Loading : GroupState()
    data class Success(val groups: List<Group>) : GroupState()
    object Empty : GroupState()
    data class GroupDetailSuccess(val group: Group) : GroupState()
    data class Error(val message: String) : GroupState()
}