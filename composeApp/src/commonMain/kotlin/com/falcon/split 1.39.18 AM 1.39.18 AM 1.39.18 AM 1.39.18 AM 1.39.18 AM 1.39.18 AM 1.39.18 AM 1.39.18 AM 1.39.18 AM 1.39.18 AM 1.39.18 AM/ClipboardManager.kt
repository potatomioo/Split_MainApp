package com.falcon.split

expect object ClipboardManager {
    fun copyToClipboard(text: String)
    fun getFromClipboard(): String?
}