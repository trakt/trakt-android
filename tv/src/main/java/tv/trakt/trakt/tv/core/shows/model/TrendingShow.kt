package tv.trakt.trakt.tv.core.shows.model

import androidx.compose.runtime.Immutable

@Immutable
internal data class TrendingShow(
    val watchers: Int,
    val show: Show,
)
