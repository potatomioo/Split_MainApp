package com.falcon.split.utils

enum class NetworkError : Error {
    REQUEST_TIMEOUT,
    UNAUTHORIZED,
    FORBIDDEN,
    CONFLICT,
    TOO_MANY_REQUESTS,
    NO_INTERNET,
    PAYLOAD_TOO_LARGE,
    SERVER_ERROR,
    SERIALIZATION,
    UNKNOWN;
}