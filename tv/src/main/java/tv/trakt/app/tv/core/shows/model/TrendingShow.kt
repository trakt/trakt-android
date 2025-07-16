package tv.trakt.app.tv.core.shows.model

import androidx.compose.runtime.Immutable

@Immutable
internal data class TrendingShow(
    val watchers: Int,
    val show: Show,
)
