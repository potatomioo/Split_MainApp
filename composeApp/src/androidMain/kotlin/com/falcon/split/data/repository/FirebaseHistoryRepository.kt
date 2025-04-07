package com.falcon.split.data.repository


import com.falcon.split.HistoryRepository
import com.falcon.split.presentation.history.HistoryActionType
import com.falcon.split.presentation.history.HistoryItem
import com.falcon.split.presentation.history.UserHistory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseHistoryRepository : HistoryRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override suspend fun getUserHistory(page: Int, itemsPerPage: Int): Flow<List<HistoryItem>> = callbackFlow {
        try {
            val currentUser = auth.currentUser ?: throw Exception("No user logged in")
            val userId = currentUser.uid

            println("DEBUG: Fetching history for user $userId, page $page, itemsPerPage $itemsPerPage")

            // Reference to the user's history document
            val userHistoryRef = db.collection("userHistories").document(userId)

            // Add a snapshot listener to get real-time updates
            val listener = userHistoryRef.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    println("DEBUG: Error fetching history - ${error.message}")
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot == null || !snapshot.exists()) {
                    println("DEBUG: No history document exists for user $userId")
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                try {
                    // Convert to UserHistory
                    val userHistory = snapshot.toObject(UserHistory::class.java)

                    if (userHistory == null) {
                        println("DEBUG: Failed to convert document to UserHistory")
                        trySend(emptyList())
                        return@addSnapshotListener
                    }

                    // Apply pagination
                    val startIndex = page * itemsPerPage
                    val endIndex = (page + 1) * itemsPerPage

                    // Sort history items by timestamp (newest first) and apply pagination
                    val paginatedItems = userHistory.historyItems
                        .sortedByDescending { it.timestamp }
                        .let {
                            if (startIndex < it.size) {
                                it.subList(startIndex, minOf(endIndex, it.size))
                            } else {
                                emptyList()
                            }
                        }

                    println("DEBUG: Fetched ${paginatedItems.size} history items for page $page")
                    trySend(paginatedItems)
                } catch (e: Exception) {
                    println("DEBUG: Error processing history data - ${e.message}")
                    e.printStackTrace()
                    close(e)
                }
            }

            awaitClose { listener.remove() }
        } catch (e: Exception) {
            println("DEBUG: Error setting up history listener - ${e.message}")
            e.printStackTrace()
            close(e)
        }
    }

    override suspend fun hasMoreHistory(page: Int, itemsPerPage: Int): Boolean {
        try {
            val currentUser = auth.currentUser ?: return false
            val userId = currentUser.uid

            // Get the user history document
            val userHistoryDoc = db.collection("userHistories").document(userId).get().await()

            if (!userHistoryDoc.exists()) {
                return false
            }

            val userHistory = userHistoryDoc.toObject(UserHistory::class.java) ?: return false

            // Calculate if there are more items beyond the current page
            val nextPageStartIndex = (page + 1) * itemsPerPage
            return userHistory.historyItems.size > nextPageStartIndex
        } catch (e: Exception) {
            println("DEBUG: Error checking for more history - ${e.message}")
            return false
        }
    }

    override suspend fun markHistoryItemAsRead(historyItemId: String): Result<Unit> {
        return try {
            val currentUser = auth.currentUser ?: return Result.failure(Exception("No user logged in"))
            val userId = currentUser.uid

            // Get the user history document
            val userHistoryRef = db.collection("userHistories").document(userId)

            // Use transactions to update the specific history item within the array
            db.runTransaction { transaction ->
                val userHistoryDoc = transaction.get(userHistoryRef)
                if (!userHistoryDoc.exists()) {
                    throw Exception("User history document does not exist")
                }

                val userHistory = userHistoryDoc.toObject(UserHistory::class.java)
                    ?: throw Exception("Could not convert document to UserHistory")

                // Find the index of the history item
                val index = userHistory.historyItems.indexOfFirst { it.id == historyItemId }
                if (index == -1) {
                    throw Exception("History item not found")
                }

                // Update the history item's read status
                val updatedItems = userHistory.historyItems.toMutableList()
                updatedItems[index] = updatedItems[index].copy(read = true)

                // Update the document
                transaction.update(userHistoryRef, "historyItems", updatedItems)
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            println("DEBUG: Error marking history item as read - ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun markAllHistoryAsRead(): Result<Unit> {
        return try {
            val currentUser = auth.currentUser ?: return Result.failure(Exception("No user logged in"))
            val userId = currentUser.uid

            // Get the user history document
            val userHistoryRef = db.collection("userHistories").document(userId)

            // Use transactions to update all history items
            db.runTransaction { transaction ->
                val userHistoryDoc = transaction.get(userHistoryRef)
                if (!userHistoryDoc.exists()) {
                    throw Exception("User history document does not exist")
                }

                val userHistory = userHistoryDoc.toObject(UserHistory::class.java)
                    ?: throw Exception("Could not convert document to UserHistory")

                // Mark all items as read
                val updatedItems = userHistory.historyItems.map { it.copy(read = true) }

                // Update the document
                transaction.update(userHistoryRef, "historyItems", updatedItems)
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            println("DEBUG: Error marking all history as read - ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun addHistoryItem(historyItem: HistoryItem): Result<Unit> {
        return try {
            // Validate the userId
            val userId = historyItem.actionByUserId.takeIf { it.isNotEmpty() }
                ?: return Result.failure(Exception("User ID cannot be empty"))

            println("DEBUG: Adding history item for user $userId: ${historyItem.actionType} - ${historyItem.description}")

            // Create a unique ID for the history item
            val itemWithId = historyItem.copy(id = db.collection("temp").document().id)

            // Reference to the user's history document
            val userHistoryRef = db.collection("userHistories").document(userId)

            // Add the history item to the user's history
            db.runTransaction { transaction ->
                val userHistoryDoc = transaction.get(userHistoryRef)

                if (userHistoryDoc.exists()) {
                    // Update existing document
                    transaction.update(
                        userHistoryRef,
                        "historyItems", FieldValue.arrayUnion(itemWithId),
                        "lastUpdated", FieldValue.serverTimestamp()
                    )
                } else {
                    // Create new document
                    val newUserHistory = UserHistory(
                        userId = userId,
                        historyItems = listOf(itemWithId),
                        lastUpdated = System.currentTimeMillis()
                    )
                    transaction.set(userHistoryRef, newUserHistory)
                }
            }.await()

            println("DEBUG: Successfully added history item for user $userId")
            Result.success(Unit)
        } catch (e: Exception) {
            println("DEBUG: Error adding history item - ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    // Helper methods to create history items for various actions

    suspend fun createGroupHistoryItem(
        groupId: String,
        groupName: String,
        createdByUserId: String,
        createdByUserName: String?,
        memberIds: List<String>
    ): Result<Unit> {
        try {
            println("DEBUG: Creating group history items for members: $memberIds")

            // Create a history item for each member
            memberIds.forEach { memberId ->
                if (memberId != createdByUserId) { // Skip creator for the "added you" message
                    val historyItem = HistoryItem(
                        actionType = HistoryActionType.GROUP_CREATED,
                        actionByUserId = createdByUserId,
                        actionByUserName = createdByUserName,
                        groupId = groupId,
                        groupName = groupName,
                        description = "$createdByUserName created the group \"$groupName\" and added you"
                    )

                    // Add to member's history
                    addHistoryItemToUser(memberId, historyItem)
                    println("DEBUG: Added group created history for member $memberId")
                }
            }

            // Create a history item for the creator too
            val creatorHistoryItem = HistoryItem(
                actionType = HistoryActionType.GROUP_CREATED,
                actionByUserId = createdByUserId,
                actionByUserName = createdByUserName,
                groupId = groupId,
                groupName = groupName,
                description = "You created the group \"$groupName\""
            )

            // Add to creator's history
            addHistoryItemToUser(createdByUserId, creatorHistoryItem)
            println("DEBUG: Added group created history for creator $createdByUserId")

            return Result.success(Unit)
        } catch (e: Exception) {
            println("DEBUG: Error creating group history - ${e.message}")
            e.printStackTrace()
            return Result.failure(e)
        }
    }

    suspend fun deleteGroupHistoryItem(
        groupId: String,
        groupName: String,
        deletedByUserId: String,
        deletedByUserName: String?,
        memberIds: List<String>
    ): Result<Unit> {
        try {
            println("DEBUG: Creating group deletion history for members: $memberIds")

            // Create a history item for each member
            memberIds.forEach { memberId ->
                if (memberId != deletedByUserId) { // We can include the deleter too
                    val historyItem = HistoryItem(
                        actionType = HistoryActionType.GROUP_DELETED,
                        actionByUserId = deletedByUserId,
                        actionByUserName = deletedByUserName,
                        groupId = groupId,
                        groupName = groupName,
                        description = "$deletedByUserName deleted the group \"$groupName\""
                    )

                    // Add the history item to the member's history document
                    val memberHistoryRef = db.collection("userHistories").document(memberId)

                    // Create a unique ID for the history item
                    val itemWithId = historyItem.copy(id = db.collection("temp").document().id)

                    db.runTransaction { transaction ->
                        val memberHistoryDoc = transaction.get(memberHistoryRef)

                        if (memberHistoryDoc.exists()) {
                            // Update existing document
                            transaction.update(
                                memberHistoryRef,
                                "historyItems", FieldValue.arrayUnion(itemWithId),
                                "lastUpdated", FieldValue.serverTimestamp()
                            )
                        } else {
                            // Create new document
                            val newUserHistory = UserHistory(
                                userId = memberId,
                                historyItems = listOf(itemWithId),
                                lastUpdated = System.currentTimeMillis()
                            )
                            transaction.set(memberHistoryRef, newUserHistory)
                        }
                    }.await()

                    println("DEBUG: Added group deleted history for member $memberId")
                }
            }

            return Result.success(Unit)
        } catch (e: Exception) {
            println("DEBUG: Error creating group deletion history - ${e.message}")
            e.printStackTrace()
            return Result.failure(e)
        }
    }

    suspend fun addExpenseHistoryItem(
        groupId: String,
        groupName: String,
        expenseId: String,
        expenseDescription: String,
        expenseAmount: Double,
        paidByUserId: String,
        paidByUserName: String?,
        memberIds: List<String>
    ): Result<Unit> {
        try {
            println("DEBUG: Creating expense history for members: $memberIds")

            // Create a history item for each member
            memberIds.forEach { memberId ->
                if (memberId != paidByUserId) { // We can include the payer too if desired
                    val historyItem = HistoryItem(
                        actionType = HistoryActionType.EXPENSE_ADDED,
                        actionByUserId = paidByUserId,
                        actionByUserName = paidByUserName,
                        groupId = groupId,
                        groupName = groupName,
                        expenseId = expenseId,
                        expenseAmount = expenseAmount,
                        description = "$paidByUserName added an expense \"$expenseDescription\" of ₹$expenseAmount in \"$groupName\""
                    )

                    // Add the history item to the member's history document
                    val memberHistoryRef = db.collection("userHistories").document(memberId)

                    // Create a unique ID for the history item
                    val itemWithId = historyItem.copy(id = db.collection("temp").document().id)

                    db.runTransaction { transaction ->
                        val memberHistoryDoc = transaction.get(memberHistoryRef)

                        if (memberHistoryDoc.exists()) {
                            // Update existing document
                            transaction.update(
                                memberHistoryRef,
                                "historyItems", FieldValue.arrayUnion(itemWithId),
                                "lastUpdated", FieldValue.serverTimestamp()
                            )
                        } else {
                            // Create new document
                            val newUserHistory = UserHistory(
                                userId = memberId,
                                historyItems = listOf(itemWithId),
                                lastUpdated = System.currentTimeMillis()
                            )
                            transaction.set(memberHistoryRef, newUserHistory)
                        }
                    }.await()

                    println("DEBUG: Added expense history for member $memberId")
                }
            }

            val creatorHistoryItem = HistoryItem(
                actionType = HistoryActionType.EXPENSE_ADDED,
                actionByUserId = paidByUserId,
                actionByUserName = paidByUserName,
                groupId = groupId,
                groupName = groupName,
                expenseId = expenseId,
                expenseAmount = expenseAmount,
                description = "You added an expense \"$expenseDescription\" of ₹$expenseAmount in \"$groupName\""
            )

            // Add to creator's history
            addHistoryItemToUser(paidByUserId, creatorHistoryItem)
            println("DEBUG: Added group created history for creator $paidByUserId")

            return Result.success(Unit)
        } catch (e: Exception) {
            println("DEBUG: Error creating expense history - ${e.message}")
            e.printStackTrace()
            return Result.failure(e)
        }
    }

    suspend fun settlementRequestHistoryItem(
        groupId: String,
        groupName: String,
        settlementId: String,
        settlementAmount: Double,
        fromUserId: String,
        fromUserName: String?,
        toUserId: String,
        toUserName: String?
    ): Result<Unit> {
        try {
            println("DEBUG: Creating settlement request history for users $fromUserId and $toUserId")

            // Create a history item for the recipient
            val recipientHistoryItem = HistoryItem(
                actionType = HistoryActionType.SETTLEMENT_REQUESTED,
                actionByUserId = fromUserId,
                actionByUserName = fromUserName,
                groupId = groupId,
                groupName = groupName,
                settlementId = settlementId,
                settlementAmount = settlementAmount,
                targetUserId = toUserId,
                targetUserName = toUserName,
                description = "$fromUserName requested a settlement of ₹$settlementAmount from you in \"$groupName\""
            )

            // Add to recipient's history
            addHistoryItemToUser(toUserId, recipientHistoryItem)
            println("DEBUG: Added settlement request history for recipient $toUserId")

            // Create a history item for the requester too
            val requesterHistoryItem = HistoryItem(
                actionType = HistoryActionType.SETTLEMENT_REQUESTED,
                actionByUserId = fromUserId,
                actionByUserName = fromUserName,
                groupId = groupId,
                groupName = groupName,
                settlementId = settlementId,
                settlementAmount = settlementAmount,
                targetUserId = toUserId,
                targetUserName = toUserName,
                description = "You requested a settlement of ₹$settlementAmount from $toUserName in \"$groupName\""
            )

            // Add to requester's history
            addHistoryItemToUser(fromUserId, requesterHistoryItem)
            println("DEBUG: Added settlement request history for requester $fromUserId")

            return Result.success(Unit)
        } catch (e: Exception) {
            println("DEBUG: Error creating settlement request history - ${e.message}")
            e.printStackTrace()
            return Result.failure(e)
        }
    }


    suspend fun settlementCompletedHistoryItem(
        groupId: String,
        groupName: String,
        settlementId: String,
        settlementAmount: Double,
        fromUserId: String,
        fromUserName: String?,
        toUserId: String,
        toUserName: String?,
        approved: Boolean
    ): Result<Unit> {
        try {
            println("DEBUG: Creating settlement completion history for user $fromUserId")

            val actionType = if (approved)
                HistoryActionType.SETTLEMENT_APPROVED
            else
                HistoryActionType.SETTLEMENT_DECLINED

            val description = if (approved)
                "$toUserName approved your settlement of ₹$settlementAmount in \"$groupName\""
            else
                "$toUserName declined your settlement of ₹$settlementAmount in \"$groupName\""

            // Create a history item for the initiator (fromUser)
            val historyItem = HistoryItem(
                actionType = actionType,
                actionByUserId = toUserId,
                actionByUserName = toUserName,
                groupId = groupId,
                groupName = groupName,
                settlementId = settlementId,
                settlementAmount = settlementAmount,
                targetUserId = fromUserId,
                targetUserName = fromUserName,
                description = description
            )

            // Add the history item to the initiator's history document
            val fromUserHistoryRef = db.collection("userHistories").document(fromUserId)

            // Create a unique ID for the history item
            val itemWithId = historyItem.copy(id = db.collection("temp").document().id)

            db.runTransaction { transaction ->
                val fromUserHistoryDoc = transaction.get(fromUserHistoryRef)

                if (fromUserHistoryDoc.exists()) {
                    // Update existing document
                    transaction.update(
                        fromUserHistoryRef,
                        "historyItems", FieldValue.arrayUnion(itemWithId),
                        "lastUpdated", FieldValue.serverTimestamp()
                    )
                } else {
                    // Create new document
                    val newUserHistory = UserHistory(
                        userId = fromUserId,
                        historyItems = listOf(itemWithId),
                        lastUpdated = System.currentTimeMillis()
                    )
                    transaction.set(fromUserHistoryRef, newUserHistory)
                }
            }.await()

            println("DEBUG: Added settlement completion history for user $fromUserId")
            return Result.success(Unit)
        } catch (e: Exception) {
            println("DEBUG: Error creating settlement completion history - ${e.message}")
            e.printStackTrace()
            return Result.failure(e)
        }
    }

    private suspend fun addHistoryItemToUser(userId: String, historyItem: HistoryItem) {
        // Create a unique ID for the history item
        val itemWithId = historyItem.copy(id = db.collection("temp").document().id)

        // Add the history item to the user's history document
        val userHistoryRef = db.collection("userHistories").document(userId)

        db.runTransaction { transaction ->
            val userHistoryDoc = transaction.get(userHistoryRef)

            if (userHistoryDoc.exists()) {
                // Update existing document
                transaction.update(
                    userHistoryRef,
                    "historyItems", FieldValue.arrayUnion(itemWithId),
                    "lastUpdated", FieldValue.serverTimestamp()
                )
            } else {
                // Create new document
                val newUserHistory = UserHistory(
                    userId = userId,
                    historyItems = listOf(itemWithId),
                    lastUpdated = System.currentTimeMillis()
                )
                transaction.set(userHistoryRef, newUserHistory)
            }
        }.await()
    }

    suspend fun memberAddedHistoryItem(
        groupId: String,
        groupName: String,
        addedByUserId: String,
        addedByUserName: String?,
        newMemberId: String,
        newMemberName: String?
    ): Result<Unit> {
        try {
            println("DEBUG: Creating member added history for user $newMemberId")

            // Create a history item for the new member
            val historyItem = HistoryItem(
                actionType = HistoryActionType.MEMBER_ADDED,
                actionByUserId = addedByUserId,
                actionByUserName = addedByUserName,
                groupId = groupId,
                groupName = groupName,
                targetUserId = newMemberId,
                targetUserName = newMemberName,
                description = "$addedByUserName added you to the group \"$groupName\""
            )

            // Add the history item to the new member's history document
            val newMemberHistoryRef = db.collection("userHistories").document(newMemberId)

            // Create a unique ID for the history item
            val itemWithId = historyItem.copy(id = db.collection("temp").document().id)

            db.runTransaction { transaction ->
                val newMemberHistoryDoc = transaction.get(newMemberHistoryRef)

                if (newMemberHistoryDoc.exists()) {
                    // Update existing document
                    transaction.update(
                        newMemberHistoryRef,
                        "historyItems", FieldValue.arrayUnion(itemWithId),
                        "lastUpdated", FieldValue.serverTimestamp()
                    )
                } else {
                    // Create new document
                    val newUserHistory = UserHistory(
                        userId = newMemberId,
                        historyItems = listOf(itemWithId),
                        lastUpdated = System.currentTimeMillis()
                    )
                    transaction.set(newMemberHistoryRef, newUserHistory)
                }
            }.await()

            println("DEBUG: Added member added history for user $newMemberId")
            return Result.success(Unit)
        } catch (e: Exception) {
            println("DEBUG: Error creating member added history - ${e.message}")
            e.printStackTrace()
            return Result.failure(e)
        }
    }
}