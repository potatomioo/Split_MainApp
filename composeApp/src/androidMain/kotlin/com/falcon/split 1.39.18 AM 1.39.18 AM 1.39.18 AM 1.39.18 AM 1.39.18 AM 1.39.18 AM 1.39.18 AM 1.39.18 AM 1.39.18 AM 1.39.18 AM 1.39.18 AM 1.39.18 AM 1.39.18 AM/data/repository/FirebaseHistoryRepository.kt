package com.falcon.split.data.repository

import com.falcon.split.data.Repository.HistoryRepository
import com.falcon.split.data.network.models_app.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseHistoryRepository : HistoryRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override suspend fun getUserTransactionHistory(
        limit: Int,
        offset: Int,
        fromTimestamp: Long?
    ): Flow<List<Transaction>> = callbackFlow {
        val currentUser = auth.currentUser ?: throw Exception("User not logged in")
        val userId = currentUser.uid

        // Combine expenses and settlements
        val transactions = mutableListOf<Transaction>()

        // First, get all groups the user is a member of
        val groupsQuery = db.collection("groups")
            .whereArrayContains("members.userId", userId)

        val groupsSnapshot = groupsQuery.get().await()
        val userGroups = groupsSnapshot.documents.mapNotNull {
            it.toObject(Group::class.java)?.copy(id = it.id)
        }

        // Now get expenses for each group
        userGroups.forEach { group ->
            // Query expenses
            val expensesQuery = db.collection("expenses")
                .whereEqualTo("groupId", group.id)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(100) // We'll filter later based on user involvement

            if (fromTimestamp != null) {
                expensesQuery.whereGreaterThan("timestamp", fromTimestamp)
            }

            val expensesSnapshot = expensesQuery.get().await()
            val expenses = expensesSnapshot.documents.mapNotNull { doc ->
                doc.toObject(Expense::class.java)?.copy(expenseId = doc.id)
            }

            // Filter expenses where user is involved (either paid or is in splits)
            val relevantExpenses = expenses.filter { expense ->
                expense.paidByUserId == userId ||
                        expense.splits?.any { it.userId == userId } == true
            }

            // Convert to transactions
            transactions.addAll(relevantExpenses.map { it.toTransaction(group.name) })

            // Query settlements
            val settlementsQuery = db.collection("settlements")
                .whereEqualTo("groupId", group.id)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(100)

            if (fromTimestamp != null) {
                settlementsQuery.whereGreaterThan("timestamp", fromTimestamp)
            }

            val settlementsSnapshot = settlementsQuery.get().await()
            val settlements = settlementsSnapshot.documents.mapNotNull { doc ->
                doc.toObject(Settlement::class.java)?.copy(id = doc.id)
            }

            // Filter settlements where user is involved (either sender or receiver)
            val relevantSettlements = settlements.filter {
                it.fromUserId == userId || it.toUserId == userId
            }

            // Convert to transactions
            transactions.addAll(relevantSettlements.map { it.toTransaction(group.name) })
        }

        // Sort by timestamp (newest first)
        val sortedTransactions = transactions
            .sortedByDescending { it.timestamp }
            .drop(offset)
            .take(limit)

        trySend(sortedTransactions)

        // Set up real-time listeners for future changes
        val expensesListener = db.collection("expenses")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(20)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }

                // Simply notify that data has changed
                println("Data changed, client should refresh transactions")
            }

        val settlementsListener = db.collection("settlements")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(20)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }

                // Simply notify that data has changed
                println("Data changed, client should refresh transactions")
            }

        awaitClose {
            expensesListener.remove()
            settlementsListener.remove()
        }
    }

    override suspend fun searchTransactions(query: String): Flow<List<Transaction>> = callbackFlow {
        // Get all transactions first, then filter by query
        // This is not efficient for large datasets, but works for demonstration
        val allTransactions = getUserTransactionHistory(100, 0, null)

        trySend(emptyList())

        awaitClose {}
    }

    override suspend fun filterTransactions(
        groupIds: List<String>?,
        transactionTypes: List<String>?,
        startDate: Long?,
        endDate: Long?,
        minAmount: Double?,
        maxAmount: Double?
    ): Flow<List<Transaction>> = callbackFlow {
        // Get all transactions first, then apply filters
        val allTransactions = getUserTransactionHistory(100, 0, null)

        trySend(emptyList())

        awaitClose {}
    }
}