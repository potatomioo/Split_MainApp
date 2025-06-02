package com.falcon.split.data.repository

import com.falcon.split.contact.Contact
import com.falcon.split.data.network.KtorApiClient
import com.falcon.split.data.network.models_app.Group
import com.falcon.split.data.network.models_app.GroupMember
import com.falcon.split.data.network.models_app.GroupType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable

@Serializable
data class CreateGroupRequest(
    val name: String,
    val members: List<String>, // Phone numbers
    val groupType: String = GroupType.OTHER.name
)

@Serializable
data class AddMembersRequest(
    val members: List<String> // Phone numbers
)

@Serializable
data class GroupResponse(
    val id: Int,
    val name: String,
    val createdBy: String,
    val groupType: String,
    val totalAmount: Double,
    val createdAt: Long,
    val updatedAt: Long? = null,
    val members: List<GroupMemberResponse>,
    val expenses: List<String> = emptyList()
)

@Serializable
data class GroupMemberResponse(
    val userId: String? = null,
    val phoneNumber: String,
    val name: String? = null,
    val balance: Double,
    val individualBalances: Map<String, Double> = emptyMap()
)

class GoBackendGroupRepository(private val ktorApiClient: KtorApiClient) : GroupRepository {

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

            val response: GroupResponse = ktorApiClient.post("api/groups", request)
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
            val response: List<GroupResponse> = ktorApiClient.get("api/groups/user")
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
            ktorApiClient.patch<GroupResponse>("api/groups/$groupId/members", request)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getGroupDetails(groupId: String): Flow<Group> = flow {
        try {
            val response: GroupResponse = ktorApiClient.get("api/groups/$groupId")
            val group = mapResponseToGroup(response)
            emit(group)
        } catch (e: Exception) {
            // Emit empty group or handle error as needed
        }
    }

    override suspend fun getPhoneNumberFromId(userId: String): String? {
        return try {
            val response: Map<String, String> = ktorApiClient.get("api/user/$userId/phone")
            response["phoneNumber"]
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun deleteGroup(groupId: String): Result<Unit> {
        return try {
            ktorApiClient.delete<Unit>("api/groups/$groupId")
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