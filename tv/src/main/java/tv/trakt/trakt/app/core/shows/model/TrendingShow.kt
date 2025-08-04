package tv.trakt.trakt.app.core.shows.model

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.model.Show

@Immutable
internal data class TrendingShow(
    val watchers: Int,
    val show: Show,
)
