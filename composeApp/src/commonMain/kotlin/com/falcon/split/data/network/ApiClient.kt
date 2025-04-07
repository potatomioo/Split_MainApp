@file:OptIn(InternalAPI::class, InternalAPI::class)

package com.falcon.split.data.network

import com.falcon.split.contact.Contact
import com.falcon.split.data.network.models.TransactionHistory
import com.falcon.split.data.network.models.UserModelGoogleCloudBased
import com.falcon.split.data.network.models_app.Expense
import com.falcon.split.data.network.models_app.Group
import com.falcon.split.utils.NetworkError
import com.falcon.split.utils.Result
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.HttpMethod
import io.ktor.util.InternalAPI
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.serialization.SerializationException

class ApiClient(
    private val httpClient: HttpClient
) {
    private val baseUrl = "https://news-app-95sc.onrender.com" // TODO: CHANGE THIS URL
//    private val baseUrl = "https://mock-api-project.vercel.app" // TODO: CHANGE THIS URL

    // Generic function to handle all API requests
    private suspend inline fun <reified T> makeApiCall(
        url: String,
        method: HttpMethod = HttpMethod.Get,  // Default to GET method
        params: Map<String, String>? = null,
        body: Any? = null // To be used for POST requests
    ): Result<T, NetworkError> {
        val response = try {
            when (method) {
                HttpMethod.Get -> {
                    httpClient.get(urlString = url) {
                        params?.forEach { (key, value) ->
                            parameter(key, value)
                        }
                    }
                }
                HttpMethod.Post -> {
                    httpClient.post(urlString = url) {
                        params?.forEach { (key, value) ->
                            parameter(key, value)
                        }
                        body?.let {
                            setBody(it)  // Set the body if provided
                        }
                    }
                }
                HttpMethod.Put -> {
                    httpClient.put(urlString = url) {
                        params?.forEach { (key, value) ->
                            parameter(key, value)
                        }
                        body?.let {
                            setBody(it)  // Set the body if provided
                        }
                    }
                }
                HttpMethod.Delete -> {
                    httpClient.delete(urlString = url) {
                        params?.forEach { (key, value) ->
                            parameter(key, value)
                        }
                    }
                }
                else -> throw IllegalArgumentException("Unsupported HTTP method")
            }
        } catch (e: UnresolvedAddressException) {
            return Result.Error(NetworkError.NO_INTERNET)
        } catch (e: SerializationException) {
            return Result.Error(NetworkError.SERIALIZATION)
        }
        println("Raw response body: ${response.content.readRemaining().readText()}")
        // Handle response based on status codes
        return when (response.status.value) {

            in 200..299 -> {
                try {
                    val responseBody: T = response.body()
                    Result.Success(responseBody)
                } catch (e: Exception) {
                    Result.Error(NetworkError.SERIALIZATION)
                }
            }
            401 -> Result.Error(NetworkError.UNAUTHORIZED)
            403 -> Result.Error(NetworkError.FORBIDDEN)
            409 -> Result.Error(NetworkError.CONFLICT)
            408 -> Result.Error(NetworkError.REQUEST_TIMEOUT)
            413 -> Result.Error(NetworkError.PAYLOAD_TOO_LARGE)
            429 -> Result.Error(NetworkError.TOO_MANY_REQUESTS)
            in 500..599 -> Result.Error(NetworkError.SERVER_ERROR)
            else -> {
                Result.Error(NetworkError.UNKNOWN)
            }
        }
    }



    suspend fun getUserDetailsFromGoogleAuthToken(googleToken: String): Result<UserModelGoogleCloudBased, NetworkError> {
        val url = "https://mock-api-project.vercel.app/api/auth/getUserFromGoogleToken"
        val params = mapOf("googleToken" to googleToken)
        return makeApiCall(url = url, method = HttpMethod.Get, params = params)
    }

    suspend fun getUserTransactionHistory(googleToken: String): Result<List<TransactionHistory>, NetworkError> {
        val url = "https://mock-api-project.vercel.app/api/auth/getUserTransactionHistory"
        val params = mapOf("googleToken" to googleToken)
        return makeApiCall(url = url, method = HttpMethod.Get, params = params)
    }

    suspend fun createExpense(
        userJwtToken: String,
        expense: Expense,
    ): Result<Boolean, NetworkError> {
        val url = "https://mock-api-project.vercel.app/api/auth/createExpense"
        val params = mapOf("googleToken" to userJwtToken)
        return makeApiCall(url = url, body = expense, method = HttpMethod.Get, params = params)
    }

    suspend fun getUserGroups(
        userJwtToken: String,
    ): Result<List<Group>, NetworkError> {
        val url = "https://mock-api-project.vercel.app/api/auth/getUserGroups"
        val params = mapOf("googleToken" to userJwtToken)
        return makeApiCall(url = url, method = HttpMethod.Get, params = params)
    }

    suspend fun createGroup(
        userJwtToken: String,
        listOfMembers: List<Contact>,
        groupName: String
    ): Result<Boolean, NetworkError> {
        val url = "https://mock-api-project.vercel.app/api/auth/createGroup"
        val createdBy: String = userJwtToken
        val params = mapOf("googleToken" to userJwtToken)
        return makeApiCall(url = url, method = HttpMethod.Get, body = listOfMembers, params = params)
    }
}