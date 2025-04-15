package com.falcon.split.data.repository

import com.falcon.split.data.Repository.ExpenseRepository
import com.falcon.split.data.network.models_app.Expense
import com.falcon.split.data.network.models_app.ExpenseType
import com.falcon.split.data.network.models_app.Group
import com.falcon.split.data.network.models_app.Settlement
import com.falcon.split.data.network.models_app.SettlementStatus
import com.falcon.split.domain.logics.expense.ExpenseCalculator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseExpenseRepository : ExpenseRepository {
    private val db = FirebaseFirestore.getInstance()
    private val expenseCalculator = ExpenseCalculator()
    private val historyRepository = FirebaseHistoryRepository()

    override suspend fun addExpense(
        groupId: String,
        description: String,
        amount: Double,
        expenseType: ExpenseType
    ): Result<Unit> {
        val currentUser = FirebaseAuth.getInstance().currentUser
            ?: return Result.failure(Exception("User not logged in"))

        val paidByUserId: String = currentUser.uid
        val paidByUserName: String = currentUser.displayName ?: "Unknown"

        return try {
            val groupSnapshot = db.collection("groups").document(groupId).get().await()
            val group = groupSnapshot.toObject(Group::class.java)
                ?: return Result.failure(Exception("Group not found"))

            // Generate expense ID beforehand
            val expenseRef = db.collection("expenses").document()
            val expenseId = expenseRef.id

            db.runTransaction { transaction ->
                // 1. Get the group to access its members
                val groupRef = db.collection("groups").document(groupId)
                val group = transaction.get(groupRef).toObject(Group::class.java)
                    ?: throw Exception("Group not found")

                // 2. Calculate splits
                val splits = expenseCalculator.calculateEqualSplits(
                    totalAmount = amount,
                    paidByUserId = paidByUserId,
                    groupMembers = group.members
                )

                // 3. Create the expense document
                val expenseRef = db.collection("expenses").document()
                val expense = Expense(
                    expenseId = expenseRef.id,
                    groupId = groupId,
                    description = description,
                    amount = amount,
                    type = expenseType.name,
                    paidByUserId = paidByUserId,
                    paidByUserName = paidByUserName,
                    splits = splits
                )

                // 4. Update member balances in the group
                val equalSplitAmount = amount / group.members.size

                // Update overall balances
                val updatedMembers = group.members.map { member ->
                    when (member.userId) {
                        // For the person who paid
                        paidByUserId -> {
                            val currentBalance = member.balance ?: 0.0
                            // They paid full amount but owe their share
                            member.copy(
                                balance = currentBalance + (amount - equalSplitAmount)
                            )
                        }
                        // For everyone else
                        else -> {
                            val currentBalance = member.balance ?: 0.0
                            // They owe their share
                            member.copy(
                                balance = currentBalance - equalSplitAmount
                            )
                        }
                    }
                }

                // Update individual balances
                val membersWithUpdatedIndividualBalances = expenseCalculator.updateIndividualBalances(
                    members = updatedMembers,
                    paidByUserId = paidByUserId,
                    totalAmount = amount
                )

                // 5. Get current expenses list and update with new expense ID
                val currentExpenses = group.expenses.toMutableList() ?: mutableListOf()
                currentExpenses.add(expenseRef.id)

                // 6. Perform all updates in the transaction
                transaction.set(expenseRef, expense)
                transaction.update(groupRef, mapOf(
                    "members" to membersWithUpdatedIndividualBalances,
                    "expenses" to currentExpenses,
                    "totalAmount" to (group.totalAmount ?: 0.0) + amount
                ))
            }.await()

            val memberIds = group.members.mapNotNull { it.userId }
            historyRepository.addExpenseHistoryItem(
                groupId = groupId,
                groupName = group.name,
                expenseId = expenseId,
                expenseDescription = description,
                expenseAmount = amount,
                paidByUserId = paidByUserId,
                paidByUserName = paidByUserName,
                memberIds = memberIds
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getExpensesByGroup(groupId: String): Flow<List<Expense>> = callbackFlow {
        try {
            // First get the group to access its expense IDs
            val groupListener = db.collection("groups")
                .document(groupId)
                .addSnapshotListener { groupSnapshot, groupError ->
                    if (groupError != null) {
                        close(groupError)
                        return@addSnapshotListener
                    }

                    val group = groupSnapshot?.toObject(Group::class.java)
                    if (group == null) {
                        trySend(emptyList())
                        return@addSnapshotListener
                    }

                    // If there are no expenses, return empty list
                    if (group.expenses.isEmpty()) {
                        trySend(emptyList())
                        return@addSnapshotListener
                    }

                    // Fetch all expenses by their IDs
                    db.collection("expenses")
                        .whereIn("expenseId", group.expenses)
                        .get()
                        .addOnSuccessListener { expensesSnapshot ->
                            val expenses = expensesSnapshot.documents.mapNotNull { doc ->
                                doc.toObject(Expense::class.java)?.copy(expenseId = doc.id)
                            }
                            trySend(expenses)
                        }
                        .addOnFailureListener { error ->
                            // If we can't get expenses by ID (perhaps we exceed the 'whereIn' limit)
                            // fall back to fetching expenses one by one
                            if (group.expenses.size > 10) { // whereIn has a limit of 10 items
                                fetchExpensesOneByOne(group.expenses) { fetchedExpenses ->
                                    trySend(fetchedExpenses)
                                }
                            } else {
                                close(error)
                            }
                        }
                }

            awaitClose { groupListener.remove() }
        } catch (e: Exception) {
            close(e)
            trySend(emptyList())
        }
    }

    private fun fetchExpensesOneByOne(expenseIds: List<String>, callback: (List<Expense>) -> Unit) {
        val expenses = mutableListOf<Expense>()
        var completedCount = 0

        for (expenseId in expenseIds) {
            db.collection("expenses")
                .document(expenseId)
                .get()
                .addOnSuccessListener { doc ->
                    doc.toObject(Expense::class.java)?.let { expense ->
                        expenses.add(expense.copy(expenseId = doc.id))
                    }
                    completedCount++

                    if (completedCount == expenseIds.size) {
                        callback(expenses)
                    }
                }
                .addOnFailureListener {
                    completedCount++
                    if (completedCount == expenseIds.size) {
                        callback(expenses)
                    }
                }
        }
    }

    override suspend fun getExpensesByUser(userId: String): Flow<List<Expense>> = callbackFlow {
        val listener = db.collection("expenses")
            .whereArrayContains("splits.userId", userId)
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val expenses = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Expense::class.java)?.copy(expenseId = doc.id)
                } ?: emptyList()

                trySend(expenses)
            }

        awaitClose { listener.remove() }
    }

    override suspend fun settleBalance(
        groupId: String,
        fromUserId: String,
        toUserId: String,
        amount: Double
    ): Result<Unit> {
        return try {
            val currentUser = FirebaseAuth.getInstance().currentUser
                ?: return Result.failure(Exception("User not logged in"))

            if (currentUser.uid != fromUserId) {
                return Result.failure(Exception("Only the user who owes can initiate settlement"))
            }

            val groupDoc = db.collection("groups").document(groupId).get().await()
            val group = groupDoc.toObject(Group::class.java)
                ?: return Result.failure(Exception("Group not found"))

            // Get user names for history
            val fromUserDoc = db.collection("users").document(fromUserId).get().await()
            val fromUserName = fromUserDoc.getString("name") ?: "Unknown"

            val toUserDoc = db.collection("users").document(toUserId).get().await()
            val toUserName = toUserDoc.getString("name") ?: "Unknown"

            // Generate settlement ID beforehand
            val settlementRef = db.collection("settlements").document()
            val settlementId = settlementRef.id

            db.runTransaction { transaction ->
                // 1. Get the group document
                val groupRef = db.collection("groups").document(groupId)
                val group = transaction.get(groupRef).toObject(Group::class.java)
                    ?: throw Exception("Group not found")

                // 2. Find the members involved
                val fromMemberIndex = group.members.indexOfFirst { it.userId == fromUserId }
                val toMemberIndex = group.members.indexOfFirst { it.userId == toUserId }

                if (fromMemberIndex == -1 || toMemberIndex == -1) {
                    throw Exception("One or more users not found in this group")
                }

                val fromMember = group.members[fromMemberIndex]
                val toMember = group.members[toMemberIndex]

                // 3. Check if the amount is valid
                val debtAmount = fromMember.individualBalances[toUserId] ?: 0.0
                if (debtAmount > 0 || debtAmount == 0.0) {
                    throw Exception("You don't owe this user any money")
                }

                if (amount > Math.abs(debtAmount)) {
                    throw Exception("Settlement amount exceeds the debt amount")
                }

                // 4. Create a pending settlement record
                val settlementRef = db.collection("settlements").document()
                val settlement = Settlement(
                    id = settlementId,
                    groupId = groupId,
                    fromUserId = fromUserId,
                    toUserId = toUserId,
                    amount = amount,
                    timestamp = kotlinx.datetime.Clock.System.now().toEpochMilliseconds(),
                    status = SettlementStatus.PENDING,
                    fromUserName = fromUserName,
                    toUserName = toUserName
                )

                transaction.set(settlementRef, settlement)
            }.await()

            historyRepository.settlementRequestHistoryItem(
                groupId = groupId,
                groupName = group.name,
                settlementId = settlementId,
                settlementAmount = amount,
                fromUserId = fromUserId,
                fromUserName = fromUserName,
                toUserId = toUserId,
                toUserName = toUserName
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun approveSettlement(settlementId: String): Result<Unit> {
        return try {
            val currentUser = FirebaseAuth.getInstance().currentUser
                ?: return Result.failure(Exception("User not logged in"))

            // Get settlement details for history
            val settlementDoc = db.collection("settlements").document(settlementId).get().await()
            val settlement = settlementDoc.toObject(Settlement::class.java) ?:
            return Result.failure(Exception("Settlement not found"))

            // Get group details
            val groupDoc = db.collection("groups").document(settlement.groupId).get().await()
            val group = groupDoc.toObject(Group::class.java) ?:
            return Result.failure(Exception("Group not found"))

            // Get user names
            val fromUserDoc = db.collection("users").document(settlement.fromUserId).get().await()
            val fromUserName = fromUserDoc.getString("name") ?: "Unknown"

            val toUserDoc = db.collection("users").document(settlement.toUserId).get().await()
            val toUserName = toUserDoc.getString("name") ?: "Unknown"


            historyRepository.settlementCompletedHistoryItem(
                groupId = settlement.groupId,
                groupName = group.name,
                settlementId = settlementId,
                settlementAmount = settlement.amount,
                fromUserId = settlement.fromUserId,
                fromUserName = fromUserName,
                toUserId = settlement.toUserId,
                toUserName = toUserName,
                approved = true
            )

            db.runTransaction { transaction ->
                // 1. Get the settlement document
                val settlementRef = db.collection("settlements").document(settlementId)
                val settlement = transaction.get(settlementRef).toObject(Settlement::class.java)
                    ?: throw Exception("Settlement not found")

                // 2. Verify that the current user is the recipient
                if (settlement.toUserId != currentUser.uid) {
                    throw Exception("Only the recipient can approve a settlement")
                }

                // 3. Check if settlement is pending
                if (settlement.status != SettlementStatus.PENDING) {
                    throw Exception("This settlement has already been processed")
                }

                // 4. Get the group document
                val groupRef = db.collection("groups").document(settlement.groupId)
                val group = transaction.get(groupRef).toObject(Group::class.java)
                    ?: throw Exception("Group not found")

                // 5. Find the members involved
                val fromMemberIndex = group.members.indexOfFirst { it.userId == settlement.fromUserId }
                val toMemberIndex = group.members.indexOfFirst { it.userId == settlement.toUserId }

                if (fromMemberIndex == -1 || toMemberIndex == -1) {
                    throw Exception("One or more users not found in this group")
                }

                val fromMember = group.members[fromMemberIndex]
                val toMember = group.members[toMemberIndex]

                // 6. Update the individual balances
                val fromMemberUpdatedBalances = fromMember.individualBalances.toMutableMap()
                val currentFromBalance = fromMemberUpdatedBalances[settlement.toUserId] ?: 0.0
                fromMemberUpdatedBalances[settlement.toUserId] = currentFromBalance + settlement.amount

                val toMemberUpdatedBalances = toMember.individualBalances.toMutableMap()
                val currentToBalance = toMemberUpdatedBalances[settlement.fromUserId] ?: 0.0
                toMemberUpdatedBalances[settlement.fromUserId] = currentToBalance - settlement.amount

                // 7. Update the overall balances
                val updatedFromMember = fromMember.copy(
                    balance = fromMember.balance + settlement.amount,
                    individualBalances = fromMemberUpdatedBalances
                )

                val updatedToMember = toMember.copy(
                    balance = toMember.balance - settlement.amount,
                    individualBalances = toMemberUpdatedBalances
                )

                // 8. Update the members list
                val updatedMembers = group.members.toMutableList()
                updatedMembers[fromMemberIndex] = updatedFromMember
                updatedMembers[toMemberIndex] = updatedToMember

                // 9. Update the group document
                transaction.update(groupRef, "members", updatedMembers)

                // 10. Update the settlement status
                transaction.update(settlementRef, "status", SettlementStatus.APPROVED)
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun declineSettlement(settlementId: String): Result<Unit> {
        return try {
            val currentUser = FirebaseAuth.getInstance().currentUser
                ?: return Result.failure(Exception("User not logged in"))

            val settlementDoc = db.collection("settlements").document(settlementId).get().await()
            val settlement = settlementDoc.toObject(Settlement::class.java) ?:
            return Result.failure(Exception("Settlement not found"))

            // Get group details
            val groupDoc = db.collection("groups").document(settlement.groupId).get().await()
            val group = groupDoc.toObject(Group::class.java) ?:
            return Result.failure(Exception("Group not found"))

            // Get user names
            val fromUserDoc = db.collection("users").document(settlement.fromUserId).get().await()
            val fromUserName = fromUserDoc.getString("name") ?: "Unknown"

            val toUserDoc = db.collection("users").document(settlement.toUserId).get().await()
            val toUserName = toUserDoc.getString("name") ?: "Unknown"

            // Record settlement decline in history
            historyRepository.settlementCompletedHistoryItem(
                groupId = settlement.groupId,
                groupName = group.name,
                settlementId = settlementId,
                settlementAmount = settlement.amount,
                fromUserId = settlement.fromUserId,
                fromUserName = fromUserName,
                toUserId = settlement.toUserId,
                toUserName = toUserName,
                approved = false
            )

            db.runTransaction { transaction ->
                // 1. Get the settlement document
                val settlementRef = db.collection("settlements").document(settlementId)
                val settlement = transaction.get(settlementRef).toObject(Settlement::class.java)
                    ?: throw Exception("Settlement not found")

                // 2. Verify that the current user is the recipient
                if (settlement.toUserId != currentUser.uid) {
                    throw Exception("Only the recipient can decline a settlement")
                }

                // 3. Check if settlement is pending
                if (settlement.status != SettlementStatus.PENDING) {
                    throw Exception("This settlement has already been processed")
                }

                // 4. Update the settlement status to declined
                transaction.update(settlementRef, "status", SettlementStatus.DECLINED)
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPendingSettlementsForUser(userId: String): Flow<List<Settlement>> = callbackFlow {
        println("Getting pending settlements for user: $userId")

        // We need to get settlements where this user is either the sender or receiver
        val pendingStatus = SettlementStatus.PENDING.toString()

        // This will be a combined listener
        val listener = db.collection("settlements")
            .whereEqualTo("status", pendingStatus)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    println("Error getting settlements: ${error.message}")
                    close(error)
                    return@addSnapshotListener
                }

                val allSettlements = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Settlement::class.java)?.copy(id = doc.id)
                } ?: emptyList()

                // Filter to only include settlements where this user is involved
                val relevantSettlements = allSettlements.filter {
                    it.fromUserId == userId || it.toUserId == userId
                }

                println("Total pending settlements: ${relevantSettlements.size}")
                trySend(relevantSettlements)
            }

        awaitClose { listener.remove() }
    }

    override suspend fun getSettlementHistory(groupId: String): Flow<List<Settlement>> = callbackFlow {
        val listener = db.collection("settlements")
            .whereEqualTo("groupId", groupId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val settlements = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Settlement::class.java)?.copy(id = doc.id)
                } ?: emptyList()

                trySend(settlements)
            }

        awaitClose { listener.remove() }
    }
}