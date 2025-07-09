//package com.falcon.split
//
//import com.falcon.split.data.network.models_app.Expense
//import com.falcon.split.data.network.models_app.Group
//import com.falcon.split.data.network.models_app.Settlement
//import com.falcon.split.data.network.models_app.User
//import dev.gitlive.firebase.Firebase
//import dev.gitlive.firebase.firestore.FirebaseFirestore
//import dev.gitlive.firebase.firestore.firestore
//import kotlinx.coroutines.flow.Flow
//import kotlinx.coroutines.flow.flow
//import kotlinx.datetime.Clock
//import kotlin.random.Random
//
//class FirebaseRepository {
//    private val firestore: FirebaseFirestore = Firebase.firestore
//
//    private fun generateId(): String {
//        return Clock.System.now().toEpochMilliseconds().toString() + "-" + (kotlin.random.Random.nextInt(1000000))
//    }
//
//    // User Operations
//    suspend fun createUser(user: User): Result<Unit> = try {
//        firestore.collection("users")
//            .document(user.userId)
//            .set(user)
//        Result.success(Unit)
//    } catch (e: Exception) {
//        Result.failure(e)
//    }
//
//    suspend fun getUser(userId: String): Result<User?> = try {
//        val document = firestore.collection("users")
//            .document(userId)
//            .get()
//        Result.success(document.data())
//    } catch (e: Exception) {
//        Result.failure(e)
//    }
//
//    // Group Operations
//    suspend fun createGroup(group: Group): Result<String> = try {
//        val docId = generateId()
//        val docRef = firestore.collection("groups").document(docId)
//        val newGroup = group.copy(groupId = docId)
//        docRef.set(newGroup)
//        Result.success(docId)
//    } catch (e: Exception) {
//        Result.failure(e)
//    }
//
//    suspend fun addMemberToGroup(groupId: String, userId: String): Result<Unit> = try {
//        val groupRef = firestore.collection("groups").document(groupId)
//        val currentGroup = groupRef.get().data<Group>()
//        val updatedMembers = currentGroup?.members?.toMutableList() ?: mutableListOf()
//        updatedMembers.add(userId)
//        groupRef.update(mapOf("members" to updatedMembers))
//        Result.success(Unit)
//    } catch (e: Exception) {
//        Result.failure(e)
//    }
//
//    suspend fun removeMemberFromGroup(groupId: String, userId: String): Result<Unit> = try {
//        val groupRef = firestore.collection("groups").document(groupId)
//        val currentGroup = groupRef.get().data<Group>()
//        val updatedMembers = currentGroup?.members?.toMutableList() ?: mutableListOf()
//        updatedMembers.remove(userId)
//        groupRef.update(mapOf("members" to updatedMembers))
//        Result.success(Unit)
//    } catch (e: Exception) {
//        Result.failure(e)
//    }
//
//    fun getUserGroups(userId: String): Flow<List<Group>> = flow {
//        try {
//            val snapshot = firestore.collection("groups")
//                .get()
//            val groups = snapshot.documents
//                .mapNotNull { it.data<Group>() }
//                .filter { it.members.contains(userId) }
//            emit(groups)
//        } catch (e: Exception) {
//            emit(emptyList())
//        }
//    }
//
//    // Expense Operations
//    suspend fun createExpense(expense: Expense): Result<String> = try {
//        val docId = generateId()
//        val docRef = firestore.collection("expenses").document(docId)
//        val newExpense = expense.copy(expenseId = docId)
//        docRef.set(newExpense)
//        Result.success(docId)
//    } catch (e: Exception) {
//        Result.failure(e)
//    }
//
//    fun getGroupExpenses(groupId: String): Flow<List<Expense>> = flow {
//        try {
//            val snapshot = firestore.collection("expenses")
//                .get()
//            val expenses = snapshot.documents
//                .mapNotNull { it.data<Expense>() }
//                .filter { it.groupId == groupId }
//            emit(expenses)
//        } catch (e: Exception) {
//            emit(emptyList())
//        }
//    }
//
//    // Settlement Operations
//    suspend fun createSettlement(settlement: Settlement): Result<String> = try {
//        val docId = generateId()
//        val docRef = firestore.collection("settlements").document(docId)
//        val newSettlement = settlement.copy(settlementId = docId)
//        docRef.set(newSettlement)
//        Result.success(docId)
//    } catch (e: Exception) {
//        Result.failure(e)
//    }
//
//    fun getGroupSettlements(groupId: String): Flow<List<Settlement>> = flow {
//        try {
//            val snapshot = firestore.collection("settlements")
//                .get()
//            val settlements = snapshot.documents
//                .mapNotNull { it.data<Settlement>() }
//                .filter { it.groupId == groupId }
//            emit(settlements)
//        } catch (e: Exception) {
//            emit(emptyList())
//        }
//    }
//
//    // Balance Calculations
//    suspend fun getGroupBalances(groupId: String): Map<String, Double> {
//        val balances = mutableMapOf<String, Double>()
//
//        try {
//            val snapshot = firestore.collection("expenses")
//                .get()
//            val expenses = snapshot.documents
//                .mapNotNull { it.data<Expense>() }
//                .filter { it.groupId == groupId }
//
//            expenses.forEach { expense ->
//                val paidByBalance = balances[expense.paidBy] ?: 0.0
//                balances[expense.paidBy] = paidByBalance + expense.amount
//
//                expense.splitBetween.forEach { split ->
//                    val currentBalance = balances[split.userId] ?: 0.0
//                    balances[split.userId] = currentBalance - split.amount
//                }
//            }
//        } catch (e: Exception) {
//            // Handle error
//        }
//
//        return balances
//    }
//
//    suspend fun deleteGroup(groupId: String): Result<Unit> = try {
//        firestore.collection("groups").document(groupId).delete()
//        Result.success(Unit)
//    } catch (e: Exception) {
//        Result.failure(e)
//    }
//
//    suspend fun deleteExpense(expenseId: String): Result<Unit> = try {
//        firestore.collection("expenses").document(expenseId).delete()
//        Result.success(Unit)
//    } catch (e: Exception) {
//        Result.failure(e)
//    }
//}



////Optimized File
//package com.falcon.split
//
//import com.falcon.split.data.network.models_app.*
//import dev.gitlive.firebase.Firebase
//import dev.gitlive.firebase.firestore.*
//import kotlinx.coroutines.flow.*
//import kotlinx.datetime.Clock
//import kotlin.random.Random
//
//class FirebaseRepository {
//    private val firestore: FirebaseFirestore = Firebase.firestore
//
//    // Collections as constants to avoid typos and make maintenance easier
//    companion object {
//        private const val USERS_COLLECTION = "users"
//        private const val GROUPS_COLLECTION = "groups"
//        private const val EXPENSES_COLLECTION = "expenses"
//        private const val SETTLEMENTS_COLLECTION = "settlements"
//        private const val MEMBERS_FIELD = "members"
//    }
//
//    private fun generateId(): String =
//        "${Clock.System.now().toEpochMilliseconds()}-${Random.nextInt(1000000)}"
//
//    // Extension functions for common operations
//    private suspend inline fun <reified T> FirebaseFirestore.getDocument(
//        collection: String,
//        documentId: String
//    ): Result<T?> = try {
//        val document = this.collection(collection).document(documentId).get()
//        Result.success(document.data())
//    } catch (e: Exception) {
//        Result.failure(e)
//    }
//
//    private suspend inline fun <reified T> FirebaseFirestore.setDocument(
//        collection: String,
//        documentId: String,
//        data: T
//    ): Result<Unit> = try {
//        this.collection(collection).document(documentId).set(data)
//        Result.success(Unit)
//    } catch (e: Exception) {
//        Result.failure(e)
//    }
//
//    // User Operations
//    suspend fun createUser(user: User): Result<Unit> =
//        firestore.setDocument(USERS_COLLECTION, user.userId, user)
//
//    suspend fun getUser(userId: String): Result<User?> =
//        firestore.getDocument(USERS_COLLECTION, userId)
//
//    // Group Operations
//    suspend fun createGroup(group: Group): Result<String> = try {
//        val docId = generateId()
//        val newGroup = group.copy(groupId = docId)
//        firestore.setDocument(GROUPS_COLLECTION, docId, newGroup)
//            .map { docId }
//    } catch (e: Exception) {
//        Result.failure(e)
//    }
//
//    private suspend fun updateGroupMembers(
//        groupId: String,
//        updateOperation: (List<String>) -> List<String>
//    ): Result<Unit> = try {
//        val groupRef = firestore.collection(GROUPS_COLLECTION).document(groupId)
//        val currentGroup = groupRef.get().data<Group>()
//        val updatedMembers = updateOperation(currentGroup?.members ?: emptyList())
//        groupRef.update(mapOf(MEMBERS_FIELD to updatedMembers))
//        Result.success(Unit)
//    } catch (e: Exception) {
//        Result.failure(e)
//    }
//
//    suspend fun addMemberToGroup(groupId: String, userId: String): Result<Unit> =
//        updateGroupMembers(groupId) { members -> members + userId }
//
//    suspend fun removeMemberFromGroup(groupId: String, userId: String): Result<Unit> =
//        updateGroupMembers(groupId) { members -> members - userId }
//
//    fun getUserGroups(userId: String): Flow<List<Group>> = flow {
//        try {
//            firestore.collection(GROUPS_COLLECTION)
//                .get()
//                .documents
//                .mapNotNull { it.data<Group>() }
//                .filter { userId in it.members }
//                .also { emit(it) }
//        } catch (e: Exception) {
//            emit(emptyList())
//        }
//    }
//
//    // Generic function for creating documents
//    private suspend inline fun <reified T> createDocument(
//        collection: String,
//        item: T,
//        copyWithId: (T, String) -> T
//    ): Result<String> = try {
//        val docId = generateId()
//        val docRef = firestore.collection(collection).document(docId)
//        val newItem = copyWithId(item, docId)
//        docRef.set(newItem)
//        Result.success(docId)
//    } catch (e: Exception) {
//        Result.failure(e)
//    }
//
//    // Expense Operations
//    suspend fun createExpense(expense: Expense): Result<String> =
//        createDocument(EXPENSES_COLLECTION, expense) { item, id -> item.copy(expenseId = id) }
//
//    fun getGroupExpenses(groupId: String): Flow<List<Expense>> = flow {
//        try {
//            firestore.collection(EXPENSES_COLLECTION)
//                .get()
//                .documents
//                .mapNotNull { it.data<Expense>() }
//                .filter { it.groupId == groupId }
//                .also { emit(it) }
//        } catch (e: Exception) {
//            emit(emptyList())
//        }
//    }
//
//    // Settlement Operations
//    suspend fun createSettlement(settlement: Settlement): Result<String> =
//        createDocument(SETTLEMENTS_COLLECTION, settlement) { item, id -> item.copy(settlementId = id) }
//
//    fun getGroupSettlements(groupId: String): Flow<List<Settlement>> = flow {
//        try {
//            firestore.collection(SETTLEMENTS_COLLECTION)
//                .get()
//                .documents
//                .mapNotNull { it.data<Settlement>() }
//                .filter { it.groupId == groupId }
//                .also { emit(it) }
//        } catch (e: Exception) {
//            emit(emptyList())
//        }
//    }
//
//    // Balance Calculations with more functional approach
//    suspend fun getGroupBalances(groupId: String): Map<String, Double> = try {
//        firestore.collection(EXPENSES_COLLECTION)
//            .get()
//            .documents
//            .mapNotNull { it.data<Expense>() }
//            .filter { it.groupId == groupId }
//            .fold(mutableMapOf<String, Double>()) { balances, expense ->
//                balances.apply {
//                    // Add amount to person who paid
//                    this[expense.paidByUserId] = (this[expense.paidByUserId] ?: 0.0) + expense.amount
//                    // Subtract split amounts
//                    expense.splitBetween.forEach { split ->
//                        this[split.userId] = (this[split.userId] ?: 0.0) - split.amount
//                    }
//                }
//            }
//    } catch (e: Exception) {
//        emptyMap()
//    }
//
//    // Delete Operations
//    private suspend fun deleteDocument(collection: String, documentId: String): Result<Unit> = try {
//        firestore.collection(collection).document(documentId).delete()
//        Result.success(Unit)
//    } catch (e: Exception) {
//        Result.failure(e)
//    }
//
//    suspend fun deleteGroup(groupId: String): Result<Unit> =
//        deleteDocument(GROUPS_COLLECTION, groupId)
//
//    suspend fun deleteExpense(expenseId: String): Result<Unit> =
//        deleteDocument(EXPENSES_COLLECTION, expenseId)
//}