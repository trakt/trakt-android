package tv.trakt.trakt.common.data.dto

import kotlinx.serialization.Serializable
import tv.trakt.trakt.common.networking.ShowCalendarsDto

@Serializable
data class TrendingShowDto(
    val watchers: Int,
    val show: ShowCalendarsDto,
)
