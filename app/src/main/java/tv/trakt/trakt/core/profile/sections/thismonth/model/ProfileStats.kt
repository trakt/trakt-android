package tv.trakt.trakt.core.profile.sections.thismonth.model

import androidx.compose.runtime.Immutable

/**
 * @property allTime All-time stats beyond the local watch counts. Null until they are loaded.
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
    val allTime: AllTimeStats? = null,
)
