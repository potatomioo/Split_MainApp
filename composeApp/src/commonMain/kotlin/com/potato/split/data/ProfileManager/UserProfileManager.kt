package com.potato.split.data.ProfileManager

interface UserProfileManager {
    suspend fun updateUserUpiId(upiId: String): Result<Unit>
}