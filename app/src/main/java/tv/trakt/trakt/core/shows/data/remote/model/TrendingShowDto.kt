package tv.trakt.trakt.core.shows.data.remote.model

import kotlinx.serialization.Serializable
import tv.trakt.trakt.common.networking.ShowCalendarsDto

@Serializable
internal data class TrendingShowDto(
    val watchers: Int,
    val show: ShowCalendarsDto,
)
