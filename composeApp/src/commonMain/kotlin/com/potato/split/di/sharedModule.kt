package com.potato.split.di

import org.koin.dsl.module

// Common Koin Module for KMM
val sharedModule = module {
//    single<NewsRepository> { NewsRepositoryImpl() } // Providing Repository //TODO
//    factory { NewsViewModel(get()) } // Providing ViewModel
}
