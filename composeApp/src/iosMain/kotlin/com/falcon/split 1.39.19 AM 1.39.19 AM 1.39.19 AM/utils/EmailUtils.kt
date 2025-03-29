// src/iosMain/kotlin/com/falcon/split/utils/EmailUtils.kt
package com.falcon.split.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.Foundation.*
import platform.UIKit.*

actual class EmailUtils {
    actual fun sendEmail(to: String, subject: String, body: String) {
        val urlString = "mailto:$to?subject=${encodeUrl(subject)}&body=${encodeUrl(body)}"
        UIApplication.sharedApplication.openURL(NSURL.URLWithString(urlString)!!)
    }

    private fun encodeUrl(text: String): String {
        return NSString.create(string = text)
            .stringByAddingPercentEncodingWithAllowedCharacters(
                NSCharacterSet.URLQueryAllowedCharacterSet()
            ) ?: text
    }
}


@Composable
actual fun rememberEmailUtils(): EmailUtils {
    return remember { EmailUtils() }
}