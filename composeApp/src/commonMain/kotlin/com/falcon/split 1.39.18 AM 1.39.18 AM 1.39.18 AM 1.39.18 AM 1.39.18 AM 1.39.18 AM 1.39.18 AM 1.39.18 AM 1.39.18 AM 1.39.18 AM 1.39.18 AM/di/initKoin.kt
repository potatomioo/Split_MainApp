package com.falcon.split.di

import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module

fun initKoin() {
    startKoin {
        modules(sharedModule)
    }
}