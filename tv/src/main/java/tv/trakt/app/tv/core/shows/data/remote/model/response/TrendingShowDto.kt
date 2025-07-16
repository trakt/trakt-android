package tv.trakt.app.tv.core.shows.data.remote.model.response

import kotlinx.serialization.Serializable
import tv.trakt.app.tv.networking.openapi.ShowDto

@Serializable
internal data class TrendingShowDto(
    val watchers: Int,
    val show: ShowDto,
)
