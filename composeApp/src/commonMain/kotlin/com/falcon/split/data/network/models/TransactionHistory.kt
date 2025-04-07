package com.falcon.split.data.network.models

import kotlinx.serialization.Serializable

@Serializable
data class TransactionHistory(
    val transactionID: String,
    val date: String,
    val amount: String,
    val groupName: String,
    val userNeedToPayOrNot: Boolean
)