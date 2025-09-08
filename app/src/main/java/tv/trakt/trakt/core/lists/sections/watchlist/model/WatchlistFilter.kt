package tv.trakt.trakt.core.lists.sections.watchlist.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import tv.trakt.trakt.resources.R

enum class WatchlistFilter(
    @param:StringRes val displayRes: Int,
    @param:DrawableRes val iconRes: Int,
) {
    MEDIA(
        displayRes = R.string.button_text_toggle_search_media,
        iconRes = R.drawable.ic_shows_movies,
    ),
    SHOWS(
        displayRes = R.string.button_text_shows,
        iconRes = R.drawable.ic_shows_off,
    ),
    MOVIES(
        displayRes = R.string.button_text_movies,
        iconRes = R.drawable.ic_movies_off,
    ),
}
