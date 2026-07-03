package tv.trakt.trakt.app.core.shows.data.remote.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import tv.trakt.trakt.common.networking.ShowCalendarsDto

@Serializable
internal data class AnticipatedShowDto(
    @SerialName("list_count") val listCount: Int,
    val show: ShowCalendarsDto,
)
