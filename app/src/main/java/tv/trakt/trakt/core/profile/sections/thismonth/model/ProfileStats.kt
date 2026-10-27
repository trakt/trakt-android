package tv.trakt.trakt.core.profile.sections.thismonth.model

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.model.UserStats

/**
 * @property allTime Full all-time stats from the API. Only VIP accounts get them, so it is null
 * for free accounts, which fall back to the local counts.
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
