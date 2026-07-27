package tv.trakt.trakt.core.summary.shows.features.seasons.model

import tv.trakt.trakt.resources.R

internal enum class SeasonsMode(
    val displayRes: Int,
    val onIcon: Int,
    val offIcon: Int,
) {
    Episodes(R.string.tab_text_seasons_episodes, R.drawable.ic_play, R.drawable.ic_play),
    Info(R.string.tab_text_seasons_info, R.drawable.ic_info, R.drawable.ic_info),
    Reviews(R.string.tab_text_seasons_reviews, R.drawable.ic_comment, R.drawable.ic_comment),
}
