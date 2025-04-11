package com.falcon.split.AndroidUserManager

import android.util.Log
import com.falcon.split.data.FirestoreManager
import com.falcon.split.data.ProfileManager.UserProfileManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.FieldValue
import kotlinx.coroutines.tasks.await

// In androidMain
class AndroidUserProfileManager(private val firestoreManager: FirestoreManager) : UserProfileManager {
    override suspend fun updateUserUpiId(upiId: String): Result<Unit> {
        return try {
            // Get current Firebase auth user
            val currentUser = FirebaseAuth.getInstance().currentUser
                ?: return Result.failure(Exception("No authenticated user found"))

            println("DEBUG: Starting UPI update for user: ${currentUser.uid}")

            // Reference to the user document
            val db = FirebaseFirestore.getInstance()
            val userRef = db.collection("users").document(currentUser.uid)

            // Check if document exists first
            val documentSnapshot = userRef.get().await()
            if (!documentSnapshot.exists()) {
                println("DEBUG: User document doesn't exist, creating new document")
                // Create a new document if it doesn't exist
                val userData = hashMapOf(
                    "uid" to currentUser.uid,
                    "upiId" to upiId,
                    "updatedAt" to null
                )
                userRef.set(userData).await()
            } else {
                println("DEBUG: Updating existing document with UPI ID: $upiId")
                // Update existing document
                userRef.update("upiId", upiId).await()
            }

            println("DEBUG: UPI ID update successful")
            Result.success(Unit)
        } catch (e: Exception) {
            println("DEBUG: UPI update failed with exception: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }
}