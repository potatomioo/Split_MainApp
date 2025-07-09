package com.potato.split

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform