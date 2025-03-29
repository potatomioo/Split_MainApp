package com.falcon.split


import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent

@SuppressLint("StaticFieldLeak")
actual object PlatformShare {
    private lateinit var context: Context

    fun init(context: Context) {
        this.context = context
    }

    actual fun shareText(text: String) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val shareIntent = Intent.createChooser(sendIntent, null).apply {
            // Add FLAG_ACTIVITY_NEW_TASK flag to chooser intent as well
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(shareIntent)
    }
}