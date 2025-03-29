package com.falcon.split

import platform.UIKit.UIPasteboard

actual object ClipboardManager {
    actual fun copyToClipboard(text: String) {
        UIPasteboard.generalPasteboard.string = text
    }

    actual fun getFromClipboard(): String? {
        return UIPasteboard.generalPasteboard.string
    }
}