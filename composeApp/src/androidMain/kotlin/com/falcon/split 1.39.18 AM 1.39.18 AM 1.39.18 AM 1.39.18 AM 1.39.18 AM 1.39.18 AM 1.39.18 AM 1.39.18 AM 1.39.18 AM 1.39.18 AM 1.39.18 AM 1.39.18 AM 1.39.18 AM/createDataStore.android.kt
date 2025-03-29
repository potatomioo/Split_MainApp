package com.falcon.split

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.falcon.split.utils.DATA_STORE_FILE_NAME
import com.falcon.split.utils.DataStoreManager

fun createDataStore(context: Context): DataStore<Preferences> {
    return DataStoreManager.getDataStore {
        context.filesDir.resolve(DATA_STORE_FILE_NAME).absolutePath
    }
}