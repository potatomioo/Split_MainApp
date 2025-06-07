package com.falcon.split.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.falcon.split.contact.Contact
import com.falcon.split.data.network.KtorApiClient
import com.falcon.split.data.network.models_app.Group
import com.falcon.split.data.network.models_app.GroupMember
import com.falcon.split.data.network.models_app.GroupType
import com.falcon.split.getToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateGroupRequest(
    @SerialName("name") val name: String,
    @SerialName("members") val members: List<String>, // Phone numbers
    @SerialName("groupType") val groupType: String = GroupType.OTHER.name
)

@Serializable
data class AddMembersRequest(
    @SerialName("members") val members: List<String> // Phone numbers
)

@Serializable
data class GroupResponse(
    @SerialName("id") val id: Int? = null,
    @SerialName("name") val name: String = "",
    @SerialName("createdBy") val createdBy: String = "",
    @SerialName("groupType") val groupType: String = GroupType.OTHER.name,
    @SerialName("totalAmount") val totalAmount: Double = 0.0,
    @SerialName("createdAt") val createdAt: Long = 0L,
    @SerialName("updatedAt") val updatedAt: Long? = null,
    @SerialName("members") val members: List<GroupMemberResponse> = emptyList(),
    @SerialName("expenses") val expenses: List<String> = emptyList()
)

@Serializable
data class GroupMemberResponse(
    @SerialName("userId") val userId: String? = null,
    @SerialName("phoneNumber") val phoneNumber: String,
    @SerialName("name") val name: String? = null,
    @SerialName("balance") val balance: Double,
    @SerialName("individualBalances") val individualBalances: Map<String, Double> = emptyMap()
)

class GoBackendGroupRepository(
    private val ktorApiClient: KtorApiClient,
    private val dataStore: DataStore<Preferences>
) : GroupRepository {

    override suspend fun createGroup(
        name: String,
        members: List<Contact>,
        groupType: GroupType
    ): Result<Group> {
        return try {
            val phoneNumbers = members.map { contact ->
                // Extract last 10 digits like Firebase implementation
                extractLast10Digits(contact.contactNumber)
            }

            val request = CreateGroupRequest(
                name = name,
                members = phoneNumbers,
                groupType = groupType.name
            )

            val token = getToken(dataStore)
            val response: GroupResponse = ktorApiClient.post("api/groups", token, request)
            val group = mapResponseToGroup(response)
            Result.success(group)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getGroupsByUser(userId: String): Flow<List<Group>> = flow {
        // This implementation is for compatibility but typically not used
        // as the getCurrentUserGroups is the main method
        emit(emptyList())
    }

    override suspend fun getCurrentUserGroups(): Flow<List<Group>> = flow {
        try {
            val token = getToken(dataStore)
            val response: List<GroupResponse> = ktorApiClient.get("api/groups/user", token)
            val groups = response.map { mapResponseToGroup(it) }
            emit(groups)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun addMembersToGroup(
        groupId: String,
        memberPhoneNumbers: List<String>
    ): Result<Unit> {
        return try {
            val normalizedNumbers = memberPhoneNumbers.map { extractLast10Digits(it) }
            val request = AddMembersRequest(normalizedNumbers)
            val token = getToken(dataStore)
            ktorApiClient.patch<GroupResponse>("api/groups/$groupId/members", token, request)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getGroupDetails(groupId: String): Flow<Group> = flow {
        try {
            val token = getToken(dataStore)
            val response: GroupResponse = ktorApiClient.get("api/groups/$groupId", token)
            val group = mapResponseToGroup(response)
            emit(group)
        } catch (e: Exception) {
            // Emit empty group or handle error as needed
        }
    }

    override suspend fun getPhoneNumberFromId(userId: String): String? {
        return try {
            val token = getToken(dataStore)
            val response: Map<String, String> = ktorApiClient.get("api/user/$userId/phone", token)
            response["phoneNumber"]
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun deleteGroup(groupId: String): Result<Unit> {
        return try {
            val token = getToken(dataStore)
            ktorApiClient.delete<Unit>("api/groups/$groupId", token)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun mapResponseToGroup(response: GroupResponse): Group {
        val members = response.members.map { memberResponse ->
            GroupMember(
                userId = memberResponse.userId,
                phoneNumber = memberResponse.phoneNumber,
                name = memberResponse.name,
                balance = memberResponse.balance,
                individualBalances = memberResponse.individualBalances
            )
        }

        return Group(
            id = response.id.toString(),
            name = response.name,
            createdBy = response.createdBy,
            members = members,
            groupType = response.groupType,
            createdAt = response.createdAt,
            updatedAt = response.updatedAt,
            totalAmount = response.totalAmount,
            expenses = response.expenses
        )
    }

    private fun extractLast10Digits(phoneNumber: String): String {
        // Remove all non-digit characters
        val digitsOnly = phoneNumber.replace(Regex("[^0-9]"), "")

        // Take the last 10 digits or the entire string if less than 10 digits
        return if (digitsOnly.length > 10) {
            digitsOnly.substring(digitsOnly.length - 10)
        } else {
            digitsOnly
        }
    }
}
