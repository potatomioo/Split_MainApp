package com.falcon.split.data.network.models_app

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.DrawableResource
import split.composeapp.generated.resources.GroupPic
import split.composeapp.generated.resources.Group_Entertainment
import split.composeapp.generated.resources.Group_Home
import split.composeapp.generated.resources.Group_Office
import split.composeapp.generated.resources.Group_Others
import split.composeapp.generated.resources.Group_Sports
import split.composeapp.generated.resources.Group_trip
import split.composeapp.generated.resources.Res

@Serializable
data class Group(
    val id: String = "",
    val name: String = "",
    val createdBy: String = "",
    val members: List<GroupMember> = emptyList(),
    val groupType: String = GroupType.OTHER.name,
    val createdAt: Long = Clock.System.now().toEpochMilliseconds(),
    val updatedAt: Long? = null,
    val totalAmount: Double? = 0.0,
    val expenses : List<String> = emptyList()
    )

enum class GroupType(val displayName: String, val iconRes: DrawableResource) {
    HOME("Home", Res.drawable.Group_Home),
    TRIP("Trip", Res.drawable.Group_trip),
    OFFICE("Office", Res.drawable.Group_Office),
    SPORTS("Sports", Res.drawable.Group_Sports),
    ENTERTAINMENT("Entertainment", Res.drawable.Group_Entertainment),
    OTHER("Other", Res.drawable.Group_Others);

    companion object {
        fun fromString(value: String?): GroupType {
            return values().find { it.name == value } ?: OTHER
        }
    }
}