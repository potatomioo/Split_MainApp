package com.falcon.split.data.auth

import android.content.SharedPreferences

class TokenManager(private val sharedPreferences: SharedPreferences) {

    companion object {
        private const val TOKEN_KEY = "auth_token"
        private const val USER_ID_KEY = "user_id"
        private const val USER_EMAIL_KEY = "user_email"
        private const val USER_NAME_KEY = "user_name"
        private const val USER_PHONE_KEY = "user_phone"
    }

    fun saveToken(token: String) {
        sharedPreferences.edit()
            .putString(TOKEN_KEY, token)
            .apply()
    }

    fun getToken(): String? {
        return sharedPreferences.getString(TOKEN_KEY, null)
    }

    fun saveUserInfo(userId: String, email: String, name: String, phone: String = "") {
        sharedPreferences.edit()
            .putString(USER_ID_KEY, userId)
            .putString(USER_EMAIL_KEY, email)
            .putString(USER_NAME_KEY, name)
            .putString(USER_PHONE_KEY, phone)
            .apply()
    }

    fun getUserId(): String? {
        return sharedPreferences.getString(USER_ID_KEY, null)
    }

    fun getUserEmail(): String? {
        return sharedPreferences.getString(USER_EMAIL_KEY, null)
    }

    fun getUserName(): String? {
        return sharedPreferences.getString(USER_NAME_KEY, null)
    }

    fun getUserPhone(): String? {
        return sharedPreferences.getString(USER_PHONE_KEY, null)
    }

    fun clearToken() {
        sharedPreferences.edit()
            .remove(TOKEN_KEY)
            .remove(USER_ID_KEY)
            .remove(USER_EMAIL_KEY)
            .remove(USER_NAME_KEY)
            .remove(USER_PHONE_KEY)
            .apply()
    }

    fun isLoggedIn(): Boolean {
        return !getToken().isNullOrEmpty() && !getUserId().isNullOrEmpty()
    }
}