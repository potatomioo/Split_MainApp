package com.falcon.split

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform