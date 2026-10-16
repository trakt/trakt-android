package tv.trakt.trakt.common.core.shows.data.remote.model

import kotlinx.serialization.Serializable
import tv.trakt.trakt.common.networking.ShowCalendarsDto

@Serializable
data class TrendingShowDto(
    val watchers: Int,
    val show: ShowCalendarsDto,
)
