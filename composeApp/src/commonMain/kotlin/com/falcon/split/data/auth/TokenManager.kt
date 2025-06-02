package com.falcon.split.data.auth

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class TokenManager(private val dataStore: DataStore<Preferences>) {

    companion object {
        private val TOKEN_KEY = stringPreferencesKey("auth_token")
        private val USER_ID_KEY = stringPreferencesKey("user_id")
        private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
        private val USER_NAME_KEY = stringPreferencesKey("user_name")
        private val USER_PHONE_KEY = stringPreferencesKey("user_phone")
    }

    suspend fun saveToken(token: String) {
        dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
        }
    }

    suspend fun getToken(): String? {
        return dataStore.data.map { preferences ->
            preferences[TOKEN_KEY]
        }.first()
    }

    suspend fun saveUserInfo(userId: String, email: String, name: String, phone: String = "") {
        dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = userId
            preferences[USER_EMAIL_KEY] = email
            preferences[USER_NAME_KEY] = name
            preferences[USER_PHONE_KEY] = phone
        }
    }

    suspend fun getUserId(): String? {
        return dataStore.data.map { preferences ->
            preferences[USER_ID_KEY]
        }.first()
    }

    suspend fun getUserEmail(): String? {
        return dataStore.data.map { preferences ->
            preferences[USER_EMAIL_KEY]
        }.first()
    }

    suspend fun getUserName(): String? {
        return dataStore.data.map { preferences ->
            preferences[USER_NAME_KEY]
        }.first()
    }

    suspend fun getUserPhone(): String? {
        return dataStore.data.map { preferences ->
            preferences[USER_PHONE_KEY]
        }.first()
    }

    suspend fun clearToken() {
        dataStore.edit { preferences ->
            preferences.remove(TOKEN_KEY)
            preferences.remove(USER_ID_KEY)
            preferences.remove(USER_EMAIL_KEY)
            preferences.remove(USER_NAME_KEY)
            preferences.remove(USER_PHONE_KEY)
        }
    }

    suspend fun isLoggedIn(): Boolean {
        val token = getToken()
        val userId = getUserId()
        return !token.isNullOrEmpty() && !userId.isNullOrEmpty()
    }
}
