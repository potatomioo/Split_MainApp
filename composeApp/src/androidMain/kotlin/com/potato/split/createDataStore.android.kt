package com.potato.split

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.potato.split.utils.DATA_STORE_FILE_NAME
import com.potato.split.utils.DataStoreManager

fun createDataStore(context: Context): DataStore<Preferences> {
    return DataStoreManager.getDataStore {
        context.filesDir.resolve(DATA_STORE_FILE_NAME).absolutePath
    }
}