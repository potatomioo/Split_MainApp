package com.falcon.split.data.network.models_app

import kotlinx.serialization.Serializable

@Serializable
data class GroupMember (
    val userId: String? = null,
    val phoneNumber: String = "",
    val name: String? =  null,
    val balance: Double = 0.0,
    val individualBalances: Map<String, Double> = mapOf(),
    val upiId : String? = null
)