package com.falcon.split.utils

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toPath
import kotlin.jvm.Synchronized

internal const val DATA_STORE_FILE_NAME = "prefs.preferences_pb"

object DataStoreManager {
    private var instance: DataStore<Preferences>? = null

    @Synchronized
    fun getDataStore(producePath: () -> String): DataStore<Preferences> {
        return instance ?: PreferenceDataStoreFactory.createWithPath(
            produceFile = { producePath().toPath() }
        ).also { instance = it }
    }
}
