package com.potato.split

actual object ClipboardManager {
    actual fun copyToClipboard(text: String) {
    }

    actual fun getFromClipboard(): String? {
        TODO("Not yet implemented")
    }
}
