@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
package com.falcon.split.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log

@SuppressLint("StaticFieldLeak")
actual object OpenLink {

    private lateinit var context: Context

    fun init(context: Context) {
        this.context = context
    }

    @SuppressLint("QueryPermissionsNeeded")
    actual fun openLink(linkToOpen: String) {
        try {
            // Create an intent with ACTION_VIEW to open the link
            val intent = Intent(Intent.ACTION_VIEW)

            // Parse the string into a Uri object and set it as data for the intent
            val uri = Uri.parse(linkToOpen)
            intent.data = uri

            // Add flags to start the activity in a new task
            // This ensures proper navigation when opening external apps
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            // Check if there's an app that can handle this intent
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            } else {
                // If no app can handle the link, you might want to open it in a browser
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(linkToOpen))
                browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(browserIntent)
            }
        } catch (e: Exception) {
            // Handle any exceptions that might occur
            // You might want to show a toast or log the error

            Log.e("LinkOpener", "Error opening link: ${e.message}", e)
        }
    }
}