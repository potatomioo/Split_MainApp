package com.falcon.split.data

import android.util.Log
import com.falcon.split.UserModelGoogleFirebaseBased
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreManager {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    suspend fun createOrUpdateUser(
        user: UserModelGoogleFirebaseBased,
        phoneNumber: String
    ): Result<Unit> {
        return try {
            // Get current Firebase auth user
            val currentUser = auth.currentUser
                ?: return Result.failure(Exception("No authenticated user found"))

            // Use the Firebase Auth UID
            val userId = currentUser.uid

            db.runTransaction { transaction ->
                // References
                val userRef = db.collection("users").document(userId)
                val phoneRef = db.collection("phoneNumbers").document(phoneNumber)

                // Check existing phone number
                val existingPhoneDoc = transaction.get(phoneRef)
                if (existingPhoneDoc.exists()) {
                    val existingUserId = existingPhoneDoc.getString("userId")
                    if (existingUserId != null && existingUserId != userId) {
                        throw IllegalStateException("Phone number already registered to another user")
                    }
                }

                // User document data
                val userData = hashMapOf(
                    "uid" to userId,
                    "phoneNumber" to phoneNumber,
                    "name" to (user.username ?: ""),
                    "email" to (user.email ?: ""),
                    "profilePictureUrl" to (user.profilePictureUrl ?: ""),
                    "updatedAt" to FieldValue.serverTimestamp()
                )

                // Add createdAt only if document doesn't exist
                val userDoc = transaction.get(userRef)
                if (!userDoc.exists()) {
                    userData["createdAt"] = FieldValue.serverTimestamp()
                }

                // Write user data
                transaction.set(userRef, userData, com.google.firebase.firestore.SetOptions.merge())

                // Write phone mapping
                transaction.set(
                    phoneRef,
                    hashMapOf(
                        "userId" to userId,
                        "phoneNumber" to phoneNumber
                    )
                )
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirestoreManager", "Error in createOrUpdateUser", e)
            Result.failure(e)
        }
    }
}