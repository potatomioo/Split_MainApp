package com.falcon.split

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.falcon.split.data.network.ApiClient
import com.falcon.split.data.network.models.UserState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.falcon.split.utils.Result
import kotlinx.coroutines.delay

class MainViewModel(
    private val apiClient: ApiClient,
    private val prefs: DataStore<Preferences>
) : ViewModel() {

    init {
        println("MainViewModel")
    }

    private val _userDetails = MutableStateFlow<UserState>(UserState.Loading)
    val userDetails: StateFlow<UserState> = _userDetails

    private var currentPage = 1 // Track the current page
    private val limit = 20 // Number of items per page
    var canLoadMore = true // Track if more data can be loaded

    fun getUserDetailsFromGoogleAuthToken(googleToken: String) {
        viewModelScope.launch {
            _userDetails.value = UserState.Loading
            val result = apiClient.getUserDetailsFromGoogleAuthToken(googleToken)
            delay(2700) // TODO: Remove this delay later
            _userDetails.value = when (result) {
                is Result.Success -> {
                    println("DEBUG_TAG" + "User ID: "+ result.data.userId)
                    UserState.Success(result.data)
                }
                is Result.Error -> {
                    println("DEBUG_TAG" + "error ID: "+ result.error.name)
                    UserState.Error(result.error)
                }
            }
        }
    }



}