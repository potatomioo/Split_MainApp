package com.falcon.split

import com.falcon.split.data.network.models_app.Expense
import com.falcon.split.data.network.models_app.Group
import com.falcon.split.data.network.models_app.Settlement
import com.falcon.split.data.network.models_app.User
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.Clock
import kotlin.random.Random

class FirebaseRepository {
    private val firestore: FirebaseFirestore = Firebase.firestore

    private fun generateId(): String {
        return Clock.System.now().toEpochMilliseconds().toString() + "-" + (kotlin.random.Random.nextInt(1000000))
    }

    // User Operations
    suspend fun createUser(user: User): Result<Unit> = try {
        firestore.collection("users")
            .document(user.userId)
            .set(user)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getUser(userId: String): Result<User?> = try {
        val document = firestore.collection("users")
            .document(userId)
            .get()
        Result.success(document.data())
    } catch (e: Exception) {
        Result.failure(e)
    }

    // Group Operations
    suspend fun createGroup(group: Group): Result<String> = try {
        val docId = generateId()
        val docRef = firestore.collection("groups").document(docId)
        val newGroup = group.copy(groupId = docId)
        docRef.set(newGroup)
        Result.success(docId)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun addMemberToGroup(groupId: String, userId: String): Result<Unit> = try {
        val groupRef = firestore.collection("groups").document(groupId)
        val currentGroup = groupRef.get().data<Group>()
        val updatedMembers = currentGroup?.members?.toMutableList() ?: mutableListOf()
        updatedMembers.add(userId)
        groupRef.update(mapOf("members" to updatedMembers))
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun removeMemberFromGroup(groupId: String, userId: String): Result<Unit> = try {
        val groupRef = firestore.collection("groups").document(groupId)
        val currentGroup = groupRef.get().data<Group>()
        val updatedMembers = currentGroup?.members?.toMutableList() ?: mutableListOf()
        updatedMembers.remove(userId)
        groupRef.update(mapOf("members" to updatedMembers))
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    fun getUserGroups(userId: String): Flow<List<Group>> = flow {
        try {
            val snapshot = firestore.collection("groups")
                .get()
            val groups = snapshot.documents
                .mapNotNull { it.data<Group>() }
                .filter { it.members.contains(userId) }
            emit(groups)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    // Expense Operations
    suspend fun createExpense(expense: Expense): Result<String> = try {
        val docId = generateId()
        val docRef = firestore.collection("expenses").document(docId)
        val newExpense = expense.copy(expenseId = docId)
        docRef.set(newExpense)
        Result.success(docId)
    } catch (e: Exception) {
        Result.failure(e)
    }

    fun getGroupExpenses(groupId: String): Flow<List<Expense>> = flow {
        try {
            val snapshot = firestore.collection("expenses")
                .get()
            val expenses = snapshot.documents
                .mapNotNull { it.data<Expense>() }
                .filter { it.groupId == groupId }
            emit(expenses)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    // Settlement Operations
    suspend fun createSettlement(settlement: Settlement): Result<String> = try {
        val docId = generateId()
        val docRef = firestore.collection("settlements").document(docId)
        val newSettlement = settlement.copy(settlementId = docId)
        docRef.set(newSettlement)
        Result.success(docId)
    } catch (e: Exception) {
        Result.failure(e)
    }

    fun getGroupSettlements(groupId: String): Flow<List<Settlement>> = flow {
        try {
            val snapshot = firestore.collection("settlements")
                .get()
            val settlements = snapshot.documents
                .mapNotNull { it.data<Settlement>() }
                .filter { it.groupId == groupId }
            emit(settlements)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    // Balance Calculations
    suspend fun getGroupBalances(groupId: String): Map<String, Double> {
        val balances = mutableMapOf<String, Double>()

        try {
            val snapshot = firestore.collection("expenses")
                .get()
            val expenses = snapshot.documents
                .mapNotNull { it.data<Expense>() }
                .filter { it.groupId == groupId }

            expenses.forEach { expense ->
                val paidByBalance = balances[expense.paidBy] ?: 0.0
                balances[expense.paidBy] = paidByBalance + expense.amount

                expense.splitBetween.forEach { split ->
                    val currentBalance = balances[split.userId] ?: 0.0
                    balances[split.userId] = currentBalance - split.amount
                }
            }
        } catch (e: Exception) {
            // Handle error
        }

        return balances
    }

    suspend fun deleteGroup(groupId: String): Result<Unit> = try {
        firestore.collection("groups").document(groupId).delete()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun deleteExpense(expenseId: String): Result<Unit> = try {
        firestore.collection("expenses").document(expenseId).delete()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}