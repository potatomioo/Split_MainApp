package com.falcon.split

import android.annotation.SuppressLint
import android.content.Context
import android.content.ClipData

@SuppressLint("StaticFieldLeak")
actual object ClipboardManager {
    private lateinit var context: Context

    fun init(context: Context) {
        this.context = context
    }

    actual fun copyToClipboard(text: String) {
        val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val clip = ClipData.newPlainText("text", text)
        clipboardManager.setPrimaryClip(clip)
    }

    actual fun getFromClipboard(): String? {
        val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val clipData = clipboardManager.primaryClip
        if (clipData != null && clipData.itemCount > 0) {
            return clipData.getItemAt(0)?.text?.toString()
        }
        return null
    }
}