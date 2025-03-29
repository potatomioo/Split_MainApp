package com.falcon.split.domain.logics.expense

import com.falcon.split.data.network.models_app.ExpenseSplit
import com.falcon.split.data.network.models_app.GroupMember

class ExpenseCalculator {
    fun calculateEqualSplits(
        totalAmount: Double,
        paidByUserId: String,
        groupMembers: List<GroupMember>
    ): List<ExpenseSplit> {
        val numberOfMembers = groupMembers.size
        val amountPerPerson = totalAmount / numberOfMembers

        return groupMembers.map { member ->
            ExpenseSplit(
                userId = member.userId ?: "",
                amount = amountPerPerson,
                settled = member.userId == paidByUserId, // Auto settle for the person who paid
                phoneNumber = member.phoneNumber
            )
        }
    }

    fun updateIndividualBalances(
        members: List<GroupMember>,
        paidByUserId: String,
        totalAmount: Double
    ): List<GroupMember> {
        val numberOfMembers = members.size
        val amountPerPerson = totalAmount / numberOfMembers

        return members.map { member ->
            val updatedBalances = member.individualBalances.toMutableMap()

            if (member.userId == paidByUserId) {
                // This is the payer - they are owed by others
                members.forEach { otherMember ->
                    if (otherMember.userId != paidByUserId && otherMember.userId != null) {
                        // Update how much otherMember owes the payer
                        val currentBalance = updatedBalances[otherMember.userId] ?: 0.0
                        updatedBalances[otherMember.userId] = currentBalance + amountPerPerson
                    }
                }
            } else {
                // This is not the payer - they owe the payer
                if (paidByUserId.isNotEmpty()) {
                    val currentBalance = updatedBalances[paidByUserId] ?: 0.0
                    updatedBalances[paidByUserId] = currentBalance - amountPerPerson
                }
            }

            member.copy(individualBalances = updatedBalances)
        }
    }
}