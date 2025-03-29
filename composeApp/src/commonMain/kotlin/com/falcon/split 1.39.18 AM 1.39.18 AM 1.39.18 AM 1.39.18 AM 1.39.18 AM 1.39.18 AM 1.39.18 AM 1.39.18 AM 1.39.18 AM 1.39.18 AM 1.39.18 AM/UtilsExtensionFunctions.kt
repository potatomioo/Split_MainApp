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
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

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

suspend fun saveFirebaseUser(prefs: DataStore<Preferences>, userModel: UserModelGoogleFirebaseBased) { // Firebase Based
    val userJson = Json.encodeToString(userModel) // Serialize UserModel to JSON string
    prefs.edit { prefs ->
        val userKey = stringPreferencesKey("user_model_firebase")
        prefs[userKey] = userJson
    }
}

suspend fun getFirebaseUserAsUserModel(prefs: DataStore<Preferences>): UserModelGoogleFirebaseBased? { // Firebase Based
    val userKey = stringPreferencesKey("user_model_firebase")
    val prefs = prefs.data.first() // Get preferences synchronously using `first`
    val userJson = prefs[userKey] ?: return null
    return Json.decodeFromString(userJson) // Deserialize JSON string back to UserModel
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