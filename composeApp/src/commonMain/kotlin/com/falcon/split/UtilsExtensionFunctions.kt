package com.falcon.split

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.falcon.split.data.network.models.UserModelGoogleCloudBased
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


private val TOKEN_KEY = stringPreferencesKey("auth_token")
private val USER_ID_KEY = stringPreferencesKey("user_id")
private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
private val USER_NAME_KEY = stringPreferencesKey("user_name")
private val USER_PHONE_KEY = stringPreferencesKey("user_phone")


suspend fun saveToken(token: String, dataStore: DataStore<Preferences>) {
    dataStore.edit { preferences ->
        preferences[TOKEN_KEY] = token
    }
}
suspend fun getToken(dataStore: DataStore<Preferences>): String? {
    return dataStore.data.map { preferences ->
        preferences[TOKEN_KEY]
    }.first()
}

suspend fun saveUserInfo(dataStore: DataStore<Preferences>, userId: String, email: String, name: String, phone: String = "") {
    dataStore.edit { preferences ->
        preferences[USER_ID_KEY] = userId
        preferences[USER_EMAIL_KEY] = email
        preferences[USER_NAME_KEY] = name
        preferences[USER_PHONE_KEY] = phone
    }
}

suspend fun getUserId(dataStore: DataStore<Preferences>): String? {
    return dataStore.data.map { preferences ->
        preferences[USER_ID_KEY]
    }.first()
}

suspend fun getUserEmail(dataStore: DataStore<Preferences>): String? {
    return dataStore.data.map { preferences ->
        preferences[USER_EMAIL_KEY]
    }.first()
}

suspend fun getUserName(dataStore: DataStore<Preferences>): String? {
    return dataStore.data.map { preferences ->
        preferences[USER_NAME_KEY]
    }.first()
}

suspend fun getUserPhone(dataStore: DataStore<Preferences>): String? {
    return dataStore.data.map { preferences ->
        preferences[USER_PHONE_KEY]
    }.first()
}

suspend fun clearToken(dataStore: DataStore<Preferences>) {
    dataStore.edit { preferences ->
        preferences.remove(TOKEN_KEY)
        preferences.remove(USER_ID_KEY)
        preferences.remove(USER_EMAIL_KEY)
        preferences.remove(USER_NAME_KEY)
        preferences.remove(USER_PHONE_KEY)
    }
}

suspend fun isLoggedIn(dataStore: DataStore<Preferences>): Boolean {
    val token = getToken(dataStore)
    val userId = getUserId(dataStore)
    return !token.isNullOrEmpty() && !userId.isNullOrEmpty()
}

suspend fun isDarkThemeEnabled(prefs: DataStore<Preferences>): Boolean {
    val darkThemeKey = booleanPreferencesKey("is_dark_theme_enabled")
    val prefs = prefs.data.first()
    return prefs[darkThemeKey] ?: false
}

suspend fun toggleDarkTheme(prefs: DataStore<Preferences>) {
    val darkThemeKey = booleanPreferencesKey("is_dark_theme_enabled")
    prefs.edit { prefs ->
        prefs[darkThemeKey] = !(prefs[darkThemeKey] ?: false)
    }
}

suspend fun saveUser(prefs: DataStore<Preferences>, userModel: UserModelGoogleCloudBased) { // Cloud Based
    val userJson = Json.encodeToString(userModel) // Serialize UserModel to JSON string
    prefs.edit { prefs ->
        val userKey = stringPreferencesKey("user_model")
        prefs[userKey] = userJson
    }
}

suspend fun getUserAsUserModel(prefs: DataStore<Preferences>): UserModelGoogleCloudBased? { // Cloud Based
    val userKey = stringPreferencesKey("user_model")
    val prefs = prefs.data.first() // Get preferences synchronously using `first`
    val userJson = prefs[userKey] ?: return null
    return Json.decodeFromString(userJson) // Deserialize JSON string back to UserModel
}

suspend fun setUserSkippedSignIn(prefs: DataStore<Preferences>) {
    val userKey = booleanPreferencesKey("is_signin_skipped_by_user")
    prefs.edit { prefs ->
        prefs[userKey] = true
    }
}

suspend fun getHaveUserSkippedSignIn(prefs: DataStore<Preferences>): Boolean {
    val userKey = booleanPreferencesKey("is_signin_skipped_by_user")
    val prefs = prefs.data.first() // Get preferences synchronously using `first`
    return prefs[userKey] ?: false
}

suspend fun deleteUser(prefs: DataStore<Preferences>) {
    prefs.edit { it.clear() }
}


fun Modifier.shimmerEffect(): Modifier = composed {
    val transition = rememberInfiniteTransition()
    val shimmerTranslate = transition.animateFloat(
        initialValue = 0f,
        targetValue = 3000f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    val shimmerBrush = Brush.linearGradient(
        colors = listOf(
            Color.LightGray.copy(alpha = 0.6f),
            Color.LightGray.copy(alpha = 0.2f),
            Color.LightGray.copy(alpha = 0.6f)
        ),
        start = Offset.Zero,
        end = Offset(x = shimmerTranslate.value, y = shimmerTranslate.value),
        tileMode = TileMode.Clamp
    )
    this.background(shimmerBrush)
}