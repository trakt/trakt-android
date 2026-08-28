package tv.trakt.trakt.core.summary.shows.features.seasons.model

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.model.Season

@Immutable
internal data class SeasonItem(
    val season: Season,
    val isWatched: Boolean = false,
    val isWatching: Boolean = false,
    val unwatchedEpisodes: Int = 0,
    val watchedEpisodes: Int = 0,
    /** Complete watches of this season, zero unless every episode was watched. */
    val plays: Int = 0,
)
