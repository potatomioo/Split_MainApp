@file:Suppress("UNCHECKED_CAST")

package com.falcon.split

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.falcon.split.data.network.ApiClient
import kotlin.reflect.KClass

class MainViewModelFactory(private val apiClient: ApiClient, private val prefs: DataStore<Preferences>) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
        return MainViewModel(apiClient, prefs) as T
    }
}

