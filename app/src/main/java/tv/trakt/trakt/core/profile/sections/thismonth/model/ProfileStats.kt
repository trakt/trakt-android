package tv.trakt.trakt.core.profile.sections.thismonth.model

import androidx.compose.runtime.Immutable

@Immutable
data class ProfileStats(
    val showsCount: Int = 0,
    val moviesCount: Int = 0,
    val episodesCount: Int = 0,
    val allShowsCount: Int = 0,
    val allMoviesCount: Int = 0,
    val allEpisodesCount: Int = 0,
)
