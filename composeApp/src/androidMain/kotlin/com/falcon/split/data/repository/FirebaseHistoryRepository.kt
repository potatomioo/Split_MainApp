package com.falcon.split.data.repository

import com.falcon.split.HistoryRepository
import com.falcon.split.presentation.history.HistoryActionType
import com.falcon.split.presentation.history.HistoryItem
import com.falcon.split.presentation.history.UserHistory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseHistoryRepository : HistoryRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Define the FirestoreUserHistory class here to handle Timestamp objects
    class FirestoreUserHistory {
        var userId: String = ""
        var historyItems: List<Map<String, Any>> = emptyList()

        // Handle both Timestamp and Long for lastUpdated
        // This is the critical part - we need to support both formats
        private var lastUpdated: Any? = null

        fun getLastUpdatedTime(): Long {
            return when (lastUpdated) {
                is Timestamp -> (lastUpdated as Timestamp).seconds * 1000
                is Long -> lastUpdated as Long
                else -> System.currentTimeMillis()
            }
        }

        fun toCommon(): UserHistory {
            return UserHistory(
                userId = userId,
                historyItems = historyItems.map { map ->
                    // Handle timestamp which can be Timestamp or Long
                    val timestamp = when (val ts = map["timestamp"]) {
                        is Timestamp -> ts.seconds * 1000
                        is Long -> ts
                        else -> System.currentTimeMillis()
                    }

                    HistoryItem(
                        id = map["id"] as? String ?: "",
                        timestamp = timestamp,
                        actionType = HistoryActionType.valueOf(map["actionType"] as String),
                        actionByUserId = map["actionByUserId"] as String,
                        actionByUserName = map["actionByUserName"] as? String,
                        groupId = map["groupId"] as? String,
                        groupName = map["groupName"] as? String,
                        expenseId = map["expenseId"] as? String,
                        expenseAmount = map["expenseAmount"] as? Double,
                        settlementId = map["settlementId"] as? String,
                        settlementAmount = map["settlementAmount"] as? Double,
                        targetUserId = map["targetUserId"] as? String,
                        targetUserName = map["targetUserName"] as? String,
                        description = map["description"] as String,
                        read = map["read"] as? Boolean ?: false
                    )
                }
            )
        }
    }

    override suspend fun getUserHistory(page: Int, itemsPerPage: Int): Flow<List<HistoryItem>> = callbackFlow {
        try {
            val currentUser = auth.currentUser ?: throw Exception("No user logged in")
            val userId = currentUser.uid

            println("DEBUG: Fetching history for user $userId, page $page, itemsPerPage $itemsPerPage")

            // Reference to the user's history document
            val userHistoryRef = db.collection("userHistories").document(userId)

            // Get the document once instead of using a listener for pagination
            val documentSnapshot = userHistoryRef.get().await()

            if (!documentSnapshot.exists()) {
                println("DEBUG: No history document exists for user $userId")
                trySend(emptyList())
                close()
                return@callbackFlow
            }

            try {
                // Convert to FirestoreUserHistory
                val firestoreUserHistory = documentSnapshot.toObject(FirestoreUserHistory::class.java)

                if (firestoreUserHistory == null) {
                    println("DEBUG: Failed to convert document to FirestoreUserHistory")
                    trySend(emptyList())
                    close()
                    return@callbackFlow
                }

                val userHistory = firestoreUserHistory.toCommon()

                // Sort all history items by timestamp (newest first)
                val sortedItems = userHistory.historyItems.sortedByDescending { it.timestamp }

                // Apply pagination
                val startIndex = page * itemsPerPage

                // Check if we're trying to access beyond the available items
                if (startIndex >= sortedItems.size) {
                    trySend(emptyList())
                    close()
                    return@callbackFlow
                }

                val endIndex = minOf(startIndex + itemsPerPage, sortedItems.size)
                val paginatedItems = sortedItems.subList(startIndex, endIndex)

                println("DEBUG: Fetched ${paginatedItems.size} history items for page $page (items ${startIndex+1}-${endIndex} of ${sortedItems.size})")
                trySend(paginatedItems)

                // Close the flow after sending the items since we're not using a listener
                close()
            } catch (e: Exception) {
                println("DEBUG: Error processing history data - ${e.message}")
                e.printStackTrace()
                close(e)
            }
        } catch (e: Exception) {
            println("DEBUG: Error setting up history data fetch - ${e.message}")
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

            val firestoreUserHistory = userHistoryDoc.toObject(FirestoreUserHistory::class.java) ?: return false
            val userHistory = firestoreUserHistory.toCommon()

            // Calculate if there are more items beyond the current page
            val nextPageStartIndex = (page + 1) * itemsPerPage
            val hasMore = userHistory.historyItems.size > nextPageStartIndex

            println("DEBUG: Has more history items? $hasMore (total: ${userHistory.historyItems.size}, nextPageStart: $nextPageStartIndex)")
            return hasMore
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

            // This is a more complex operation because we need to update a specific item in an array
            // We need to get the document, modify the array, and update it
            val document = userHistoryRef.get().await()
            if (!document.exists()) {
                return Result.failure(Exception("User history document does not exist"))
            }

            val firestoreUserHistory = document.toObject(FirestoreUserHistory::class.java)
                ?: return Result.failure(Exception("Could not convert document to FirestoreUserHistory"))

            val updatedItems = firestoreUserHistory.historyItems.map { item ->
                if (item["id"] == historyItemId) {
                    // Create a new map with the read field set to true
                    item.toMutableMap().apply {
                        this["read"] = true
                    }
                } else {
                    item
                }
            }

            // Update the document with the modified array
            userHistoryRef.update("historyItems", updatedItems).await()

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

            // Similar to markHistoryItemAsRead, but we update all items
            val document = userHistoryRef.get().await()
            if (!document.exists()) {
                return Result.failure(Exception("User history document does not exist"))
            }

            val firestoreUserHistory = document.toObject(FirestoreUserHistory::class.java)
                ?: return Result.failure(Exception("Could not convert document to FirestoreUserHistory"))

            val updatedItems = firestoreUserHistory.historyItems.map { item ->
                // Create a new map with the read field set to true
                item.toMutableMap().apply {
                    this["read"] = true
                }
            }

            // Update the document with the modified array
            userHistoryRef.update("historyItems", updatedItems).await()

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

            // Add the history item to the user's history
            addHistoryItemToUser(userId, itemWithId)

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

                    // Add to member's history
                    addHistoryItemToUser(memberId, historyItem)
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

                    // Add to member's history
                    addHistoryItemToUser(memberId, historyItem)
                    println("DEBUG: Added expense history for member $memberId")
                }
            }

            // Create a history item for the expense creator too
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
            println("DEBUG: Added expense history for creator $paidByUserId")

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

            // Add to initiator's history
            addHistoryItemToUser(fromUserId, historyItem)
            println("DEBUG: Added settlement completion history for user $fromUserId")

            return Result.success(Unit)
        } catch (e: Exception) {
            println("DEBUG: Error creating settlement completion history - ${e.message}")
            e.printStackTrace()
            return Result.failure(e)
        }
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

            // Add to new member's history
            addHistoryItemToUser(newMemberId, historyItem)
            println("DEBUG: Added member added history for user $newMemberId")

            return Result.success(Unit)
        } catch (e: Exception) {
            println("DEBUG: Error creating member added history - ${e.message}")
            e.printStackTrace()
            return Result.failure(e)
        }
    }

    private suspend fun addHistoryItemToUser(userId: String, historyItem: HistoryItem) {
        try {
            // Create a unique ID if one doesn't exist
            val itemId = historyItem.id.ifEmpty { db.collection("temp").document().id }
            val itemWithId = if (historyItem.id.isEmpty()) historyItem.copy(id = itemId) else historyItem

            // Create a Firestore-friendly map of the history item
            val historyItemMap = mapOf(
                "id" to itemWithId.id,
                "timestamp" to Timestamp(itemWithId.timestamp / 1000, 0),
                "actionType" to itemWithId.actionType.name,
                "actionByUserId" to itemWithId.actionByUserId,
                "actionByUserName" to itemWithId.actionByUserName,
                "groupId" to itemWithId.groupId,
                "groupName" to itemWithId.groupName,
                "expenseId" to itemWithId.expenseId,
                "expenseAmount" to itemWithId.expenseAmount,
                "settlementId" to itemWithId.settlementId,
                "settlementAmount" to itemWithId.settlementAmount,
                "targetUserId" to itemWithId.targetUserId,
                "targetUserName" to itemWithId.targetUserName,
                "description" to itemWithId.description,
                "read" to itemWithId.read
            )

            // Add the history item to the user's history document
            val userHistoryRef = db.collection("userHistories").document(userId)

            // Check if the document exists first
            val documentSnapshot = userHistoryRef.get().await()

            if (documentSnapshot.exists()) {
                // Update existing document - use update instead of transaction
                userHistoryRef.update(
                    "historyItems", FieldValue.arrayUnion(historyItemMap),
                    "lastUpdated", FieldValue.serverTimestamp()
                ).await()
            } else {
                // Create new document - use set instead of transaction
                val newUserHistory = mapOf(
                    "userId" to userId,
                    "historyItems" to listOf(historyItemMap),
                    "lastUpdated" to FieldValue.serverTimestamp()
                )
                userHistoryRef.set(newUserHistory).await()
            }

            println("DEBUG: Successfully added history item to user $userId")
        } catch (e: Exception) {
            println("DEBUG: Error in addHistoryItemToUser - ${e.message}")
            e.printStackTrace()
            throw e
        }
    }
}