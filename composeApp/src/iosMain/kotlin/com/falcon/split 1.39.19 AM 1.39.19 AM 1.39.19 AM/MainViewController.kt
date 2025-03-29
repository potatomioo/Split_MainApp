package com.falcon.split

import androidx.compose.runtime.remember
import androidx.compose.ui.window.ComposeUIViewController
import com.falcon.split.data.network.ApiClient
import com.falcon.split.data.network.createHttpClient
import createDataStore
import io.ktor.client.engine.darwin.Darwin

//fun MainViewController() = ComposeUIViewController {
//    App(
//        client = remember {
//            ApiClient(createHttpClient(Darwin.create()))
//        },
//        prefs = remember {
//            createDataStore()
//        },
//        onSignOut = null,
//        darkTheme = null
//    )
//}