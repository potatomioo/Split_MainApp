@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package com.falcon.split.screens.mainNavigation

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent

@SuppressLint("StaticFieldLeak")
actual object Intents {
    private lateinit var context: Context

    fun init(context: Context) {
        this.context = context
    }
    actual fun openPaytm() {
        val intent = context.packageManager.getLaunchIntentForPackage("net.one97.paytm")
        context.startActivity(intent)
    }
    actual fun openGooglePay() {
        val intent = context.packageManager.getLaunchIntentForPackage("com.google.android.apps.nbu.paisa.user")
        context.startActivity(intent)
    }

    actual fun openPhonePe() {
        val intent = context.packageManager.getLaunchIntentForPackage("com.phonepe.app")
        context.startActivity(intent)
    }

    actual fun inviteFriends() {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "Hey! I'm using SplitBill to split expenses. It makes sharing bills and expenses super easy! Join me here: https://play.google.com/store/apps/details?id=com.falcon.split")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share via")
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(shareIntent)
    }
}