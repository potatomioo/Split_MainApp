package com.falcon.split.data.repository

import com.falcon.split.contact.Contact
import com.falcon.split.data.Repository.GroupRepository
import com.falcon.split.data.network.models_app.Group
import com.falcon.split.data.network.models_app.GroupMember
import com.falcon.split.data.network.models_app.GroupType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseGroupRepository : GroupRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val historyRepository = FirebaseHistoryRepository()

    override suspend fun getPhoneNumberFromId(userId: String): String? {
        val querySnapshot = db.collection("phoneNumbers")
            .whereEqualTo("userId", userId)
            .get()
            .await()

        return querySnapshot.documents.firstOrNull()?.getString("phoneNumber")
    }

    override suspend fun createGroup(name: String, members: List<Contact>,groupType: GroupType): Result<Group> {
        return try {
            val currentUser = FirebaseAuth.getInstance().currentUser
                ?: return Result.failure(Exception("User not logged in"))

            val groupMembers = members.map { contact ->
                // Extract last 10 digits of phone number before storing
                val normalizedPhoneNumber = extractLast10Digits(contact.contactNumber)

                // Get user info from phoneNumbers collection if exists
                val userDoc = db.collection("phoneNumbers")
                    .document(normalizedPhoneNumber)
                    .get()
                    .await()

                val registeredUserId = userDoc.getString("userId")
                val registeredUserName = if (registeredUserId != null) {
                    val userProfileDoc = db.collection("users")
                        .document(registeredUserId)
                        .get()
                        .await()

                    userProfileDoc.getString("name")
                } else {
                    null
                }

                GroupMember(
                    userId = userDoc.getString("userId"),
                    phoneNumber = normalizedPhoneNumber, // Store the normalized number
                    name = registeredUserName,
                    balance = 0.0
                )
            }

            val currentUserPhoneNumber = getPhoneNumberFromId(currentUser.uid)
            // Also normalize current user's phone number
            val normalizedCurrentUserPhone = extractLast10Digits(currentUserPhoneNumber ?: "")

            val currentUserDoc = db.collection("users")
                .document(currentUser.uid)
                .get()
                .await()

            val currentUserName = currentUserDoc.getString("name") ?: currentUser.displayName
            val groupRef = db.collection("groups").document()

            val group = Group(
                id = groupRef.id,
                name = name,
                createdBy = currentUser.uid,
                members = groupMembers + GroupMember(
                    userId = currentUser.uid,
                    phoneNumber = normalizedCurrentUserPhone,
                    name = currentUserName,
                    balance = 0.0
                ),
                groupType = groupType.name,
                totalAmount = 0.0,
                updatedAt = null
            )

            groupRef.set(group).await()

            val memberIds = groupMembers.mapNotNull { it.userId }
            historyRepository.createGroupHistoryItem(
                groupId = groupRef.id,
                groupName = name,
                createdByUserId = currentUser.uid,
                createdByUserName = currentUserName,
                memberIds = memberIds,
                groupType = groupType.name
            )

            Result.success(group)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    override suspend fun getGroupsByUser(userId: String): Flow<List<Group>> = callbackFlow {
        val listener = db.collection("groups")
            .whereArrayContains("members.userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val groups = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Group::class.java)?.copy(id = doc.id)
                } ?: emptyList()

                trySend(groups)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun getCurrentUserGroups(): Flow<List<Group>> = callbackFlow {
        val currentUser = auth.currentUser ?: throw Exception("No user logged in")
        println("DEBUG: Current User ID - ${currentUser.uid}")  // Debug log

        try {
            // Query all groups and filter client-side for debugging
            val listener = db.collection("groups")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        println("DEBUG: Firestore Error - ${error.message}")
                        close(error)
                        return@addSnapshotListener
                    }

                    snapshot?.documents?.forEach { doc ->
                        // Debug print each document
                        println("DEBUG: Document ID - ${doc.id}")
                        println("DEBUG: Document Data - ${doc.data}")
                    }

                    val groups = snapshot?.documents?.mapNotNull { doc ->
                        doc.toObject(Group::class.java)?.let { group ->
                            // Debug print members
                            println("DEBUG: Group ${group.id} members - ${group.members}")

                            // Check if user is a member
                            if (group.members.any { member ->
                                    member.userId == currentUser.uid
                                }) {
                                group.copy(id = doc.id)
                            } else null
                        }
                    } ?: emptyList()

                    println("DEBUG: Final groups count - ${groups.size}")
                    trySend(groups)
                }

            awaitClose { listener.remove() }
        } catch (e: Exception) {
            println("DEBUG: Repository error - ${e.message}")
            close(e)
        }
    }

    override suspend fun addMembersToGroup(groupId: String, memberPhoneNumbers: List<String>): Result<Unit> {
        return try {
            coroutineScope {
                val groupRef = db.collection("groups").document(groupId)
                val groupDoc = groupRef.get().await()
                val group = groupDoc.toObject(Group::class.java)!!

                val newMembers = memberPhoneNumbers.map { phoneNumber ->
                    val userDoc = db.collection("phoneNumbers")
                        .document(phoneNumber)
                        .get()
                        .await()

                    GroupMember(
                        userId = userDoc.getString("userId"),
                        phoneNumber = phoneNumber,
                        name = null,
                        balance = 0.0
                    )
                }

                val updatedMembers = (group.members + newMembers).distinctBy { it.phoneNumber }
                groupRef.update("members", updatedMembers).await()

                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteGroup(groupId: String): Result<Unit> {
        return try {
            // Get the current user
            val currentUser = auth.currentUser ?: return Result.failure(Exception("User not logged in"))

            // Get the group to check permissions
            val groupDoc = db.collection("groups").document(groupId).get().await()
            val group = groupDoc.toObject(Group::class.java)
                ?: return Result.failure(Exception("Group not found"))

            // Only allow deletion by the creator
            if (group.createdBy != currentUser.uid) {
                return Result.failure(Exception("Only the group creator can delete this group"))
            }

            val currentUserDoc = db.collection("users").document(currentUser.uid).get().await()
            val currentUserName = currentUserDoc.getString("name") ?: currentUser.displayName

            // Record history before deletion
            val memberIds = group.members.mapNotNull { it.userId }
            historyRepository.deleteGroupHistoryItem(
                groupId = groupId,
                groupName = group.name,
                deletedByUserId = currentUser.uid,
                deletedByUserName = currentUserName,
                memberIds = memberIds
            )

            val expensesQuery = db.collection("expenses")
                .whereEqualTo("groupId", groupId)
                .get()
                .await()

            // Then, delete each expense document
            for (expenseDoc in expensesQuery.documents) {
                db.collection("expenses").document(expenseDoc.id).delete().await()
            }

            // Finally, delete the group document
            db.collection("groups").document(groupId).delete().await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getGroupDetails(groupId: String): Flow<Group> = callbackFlow {
        val listener = db.collection("groups")
            .document(groupId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                snapshot?.toObject(Group::class.java)?.let { group ->
                    trySend(group.copy(id = snapshot.id))
                }
            }

        awaitClose { listener.remove() }
    }
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