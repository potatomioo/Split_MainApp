// src/commonMain/kotlin/com/falcon/split/utils/EmailUtils.kt
package com.falcon.split.utils

import androidx.compose.runtime.Composable

expect class EmailUtils {
    fun sendEmail(
        to: String,
        subject: String = "",
        body: String = ""
    )
}


// Remember composable for platform-specific instantiation
@Composable
expect fun rememberEmailUtils(): EmailUtils


expect object OpenLink {
    fun openLink(linkToOpen: String)
}