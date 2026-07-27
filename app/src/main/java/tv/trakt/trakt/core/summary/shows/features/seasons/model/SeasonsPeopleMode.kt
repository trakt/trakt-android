package tv.trakt.trakt.core.summary.shows.features.seasons.model

import tv.trakt.trakt.resources.R

internal enum class SeasonsPeopleMode(
    val displayRes: Int,
    val iconRes: Int,
) {
    Cast(R.string.drawer_meta_info_cast, R.drawable.ic_cast),
    Crew(R.string.drawer_meta_info_crew, R.drawable.ic_crew),
}
