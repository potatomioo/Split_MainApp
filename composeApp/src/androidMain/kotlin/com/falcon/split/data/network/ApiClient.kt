package com.falcon.split.data.network

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class ApiClient(private val tokenProvider: () -> String?) {

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
        
        install(Auth) {
            bearer {
                loadTokens {
                    tokenProvider()?.let { token ->
                        BearerTokens(token, "")
                    }
                }
            }
        }
    }

    suspend inline fun <reified T> get(
        endpoint: String,
        parameters: Map<String, Any> = emptyMap()
    ): T {
        return httpClient.get {
            url(ApiConfig.BASE_URL + endpoint)
            parameters.forEach { (key, value) ->
                parameter(key, value.toString())
            }
        }.body()
    }

    suspend inline fun <reified T> post(
        endpoint: String,
        body: Any? = null
    ): T {
        return httpClient.post {
            url(ApiConfig.BASE_URL + endpoint)
            contentType(ContentType.Application.Json)
            body?.let { setBody(it) }
        }.body()
    }

    suspend inline fun <reified T> patch(
        endpoint: String,
        body: Any? = null
    ): T {
        return httpClient.patch {
            url(ApiConfig.BASE_URL + endpoint)
            contentType(ContentType.Application.Json)
            body?.let { setBody(it) }
        }.body()
    }

    suspend inline fun <reified T> delete(
        endpoint: String
    ): T {
        return httpClient.delete {
            url(ApiConfig.BASE_URL + endpoint)
        }.body()
    }
    
    fun close() {
        httpClient.close()
    }
}
