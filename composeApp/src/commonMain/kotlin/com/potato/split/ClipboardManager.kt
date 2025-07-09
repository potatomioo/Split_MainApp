package com.potato.split

expect object ClipboardManager {
    fun copyToClipboard(text: String)
    fun getFromClipboard(): String?
}