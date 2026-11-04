package tv.trakt.trakt.core.profile.sections.thismonth.model

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.model.UserStats

/**
 * @property allTime All-time stats from the API. Null when they are not loaded or not available,
 * in which case the local counts are used.
 */
@Immutable
data class ProfileStats(
    val showsCount: Int = 0,
    val moviesCount: Int = 0,
    val episodesCount: Int = 0,
    val playsCount: Int = 0,
    val allShowsCount: Int = 0,
    val allMoviesCount: Int = 0,
    val allEpisodesCount: Int = 0,
    val allPlaysCount: Int = 0,
    val allTime: UserStats? = null,
)
