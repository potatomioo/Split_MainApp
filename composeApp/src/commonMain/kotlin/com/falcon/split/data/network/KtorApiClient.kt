package com.falcon.split.data.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class KtorApiClient {

    @PublishedApi
    internal val httpClient = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
            })
        }
        
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.INFO
        }
        
        install(HttpTimeout) {
            requestTimeoutMillis = ApiConfig.TIMEOUT_SECONDS * 1000
            connectTimeoutMillis = ApiConfig.TIMEOUT_SECONDS * 1000
            socketTimeoutMillis = ApiConfig.TIMEOUT_SECONDS * 1000
        }
    }

    suspend inline fun <reified T> get(
        endpoint: String,
        token: String?,
        parameters: Map<String, Any> = emptyMap()
    ): T {
        return httpClient.get {
            url(ApiConfig.BASE_URL + endpoint)
            token?.let { bearerAuth(it) }
            parameters.forEach { (key, value) ->
                parameter(key, value.toString())
            }
        }.body()
    }

    suspend inline fun <reified T> post(
        endpoint: String,
        token: String?,
        body: Any? = null
    ): T {
        return httpClient.post {
            url(ApiConfig.BASE_URL + endpoint)
            contentType(ContentType.Application.Json)
            token?.let { bearerAuth(it) }
            body?.let { setBody(it) }
        }.body()
    }

    suspend inline fun <reified T> patch(
        endpoint: String,
        token: String?,
        body: Any? = null
    ): T {
        return httpClient.patch {
            url(ApiConfig.BASE_URL + endpoint)
            contentType(ContentType.Application.Json)
            token?.let { bearerAuth(it) }
            body?.let { setBody(it) }
        }.body()
    }

    suspend inline fun <reified T> delete(
        endpoint: String,
        token: String?
    ): T {
        return httpClient.delete {
            url(ApiConfig.BASE_URL + endpoint)
            token?.let { bearerAuth(it) }
        }.body()
    }
    
    fun close() {
        httpClient.close()
    }
}
