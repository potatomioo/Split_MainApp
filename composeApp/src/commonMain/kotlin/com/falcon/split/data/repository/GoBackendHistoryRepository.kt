package com.falcon.split.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.falcon.split.HistoryRepository
import com.falcon.split.data.network.KtorApiClient
import com.falcon.split.getToken
import com.falcon.split.presentation.history.HistoryActionType
import com.falcon.split.presentation.history.HistoryItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HistoryItemResponse(
    @SerialName("id") val id: String,
    @SerialName("timestamp") val timestamp: Long,
    @SerialName("actionType") val actionType: String,
    @SerialName("actionByUserId") val actionByUserId: String,
    @SerialName("actionByUserName") val actionByUserName: String? = null,
    @SerialName("groupId") val groupId: String? = null,
    @SerialName("groupName") val groupName: String? = null,
    @SerialName("groupType") val groupType: String? = null,
    @SerialName("expenseId") val expenseId: String? = null,
    @SerialName("expenseAmount") val expenseAmount: Double? = null,
    @SerialName("expenseType") val expenseType: String? = null,
    @SerialName("settlementId") val settlementId: String? = null,
    @SerialName("settlementAmount") val settlementAmount: Double? = null,
    @SerialName("targetUserId") val targetUserId: String? = null,
    @SerialName("targetUserName") val targetUserName: String? = null,
    @SerialName("description") val description: String,
    @SerialName("read") val read: Boolean
)

@Serializable
data class HasMoreResponse(
    @SerialName("hasMore") val hasMore: Boolean
)

class GoBackendHistoryRepository(
    private val ktorApiClient: KtorApiClient,
    private val dataStore: DataStore<Preferences>
) : HistoryRepository {

    override suspend fun getUserHistory(page: Int, itemsPerPage: Int): Flow<List<HistoryItem>> =
        flow {
            try {
                val parameters = mapOf(
                    "page" to page,
                    "itemsPerPage" to itemsPerPage
                )
                val token = getToken(dataStore)
                val response: List<HistoryItemResponse> =
                    ktorApiClient.get("api/history", token, parameters)
                val historyItems = response.map { mapResponseToHistoryItem(it) }
                emit(historyItems)
            } catch (e: Exception) {
                emit(emptyList())
            }
        }

    override suspend fun hasMoreHistory(page: Int, itemsPerPage: Int): Boolean {
        return try {
            val parameters = mapOf(
                "page" to page,
                "itemsPerPage" to itemsPerPage
            )
            val token = getToken(dataStore)
            val response: HasMoreResponse =
                ktorApiClient.get("api/history/hasMore", token, parameters)
            response.hasMore
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun markHistoryItemAsRead(historyId: String): Result<Unit> {
        return try {
            val token = getToken(dataStore)
            ktorApiClient.patch<Unit>("api/history/$historyId/read", token)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addHistoryItem(historyItem: HistoryItem): Result<Unit> {
        // This method is typically used internally by the backend
        // For client-side, we don't usually need to add history items directly
        return Result.success(Unit)
    }

    override suspend fun markAllHistoryAsRead(): Result<Unit> {
        return try {
            val token = getToken(dataStore)
            ktorApiClient.patch<Unit>("api/history/markAllRead", token)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getRecentHistory(limit: Int): Flow<List<HistoryItem>> = flow {
        try {
            val parameters = mapOf("limit" to limit)
            val token = getToken(dataStore)
            val response: List<HistoryItemResponse> =
                ktorApiClient.get("api/history/recent", token, parameters)
            val historyItems = response.map { mapResponseToHistoryItem(it) }
            emit(historyItems)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    private fun mapResponseToHistoryItem(response: HistoryItemResponse): HistoryItem {
        val actionType = try {
            HistoryActionType.valueOf(response.actionType)
        } catch (e: Exception) {
            HistoryActionType.GROUP_CREATED // Default fallback
        }

        return HistoryItem(
            id = response.id,
            timestamp = response.timestamp,
            actionType = actionType,
            actionByUserId = response.actionByUserId,
            actionByUserName = response.actionByUserName,
            groupId = response.groupId,
            groupName = response.groupName,
            groupType = response.groupType,
            expenseId = response.expenseId,
            expenseAmount = response.expenseAmount,
            expenseType = response.expenseType,
            settlementId = response.settlementId,
            settlementAmount = response.settlementAmount,
            targetUserId = response.targetUserId,
            targetUserName = response.targetUserName,
            description = response.description,
            read = response.read
        )
    }
}
