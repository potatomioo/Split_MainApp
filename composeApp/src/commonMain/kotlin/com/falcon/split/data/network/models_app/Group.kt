package com.falcon.split.data.network.models_app

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class Group(
    val id: String = "",
    val name: String = "",
    val createdBy: String = "",
    val members: List<GroupMember> = emptyList(),
    val createdAt: Long = Clock.System.now().toEpochMilliseconds(),
    val updatedAt: Long? = null,
    val totalAmount: Double? = 0.0,
    val expenses : List<String> = emptyList()
    )