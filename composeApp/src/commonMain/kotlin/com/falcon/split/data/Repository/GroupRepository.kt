package com.falcon.split.data.Repository

import com.falcon.split.contact.Contact
import com.falcon.split.data.network.models_app.Group
import com.falcon.split.data.network.models_app.GroupType
import kotlinx.coroutines.flow.Flow

interface GroupRepository {
    suspend fun createGroup(name: String, members: List<Contact>,groupType: GroupType = GroupType.OTHER): Result<Group>
    suspend fun getGroupsByUser(userId: String): Flow<List<Group>>
    suspend fun getCurrentUserGroups(): Flow<List<Group>>
    suspend fun addMembersToGroup(groupId: String, memberPhoneNumbers: List<String>): Result<Unit>
    suspend fun getGroupDetails(groupId: String): Flow<Group>
    suspend fun getPhoneNumberFromId(userId: String) : String?
    suspend fun deleteGroup(groupId: String): Result<Unit>
}