// src/desktopMain/kotlin/com/falcon/split/utils/EmailUtils.kt
package com.falcon.split.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.awt.Desktop
import java.net.URI

actual class EmailUtils {
    actual fun sendEmail(to: String, subject: String, body: String) {
        try {
            val mailtoUrl = "mailto:$to?subject=${java.net.URLEncoder.encode(subject, "UTF-8")}&body=${java.net.URLEncoder.encode(body, "UTF-8")}"
            Desktop.getDesktop().mail(URI(mailtoUrl))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}


@Composable
actual fun rememberEmailUtils(): EmailUtils {
    return remember { EmailUtils() }
}