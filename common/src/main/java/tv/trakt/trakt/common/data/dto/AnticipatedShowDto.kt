package tv.trakt.trakt.common.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import tv.trakt.trakt.common.networking.ShowCalendarsDto

@Serializable
data class AnticipatedShowDto(
    @SerialName("list_count") val listCount: Int,
    val show: ShowCalendarsDto,
)
