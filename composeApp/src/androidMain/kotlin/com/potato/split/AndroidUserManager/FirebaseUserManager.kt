package com.potato.split.AndroidUserManager

import com.potato.split.userManager.UserManager
import com.google.firebase.auth.FirebaseAuth

class FirebaseUserManager : UserManager {
    override fun getCurrentUserId(): String? {
        return FirebaseAuth.getInstance().currentUser?.uid
    }
}