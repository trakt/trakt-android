package tv.trakt.trakt.core.search.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import tv.trakt.trakt.resources.R

enum class SearchFilter(
    @param:DrawableRes val iconRes: Int,
    @param:StringRes val displayRes: Int,
) {
    MEDIA(
        iconRes = R.drawable.ic_shows_movies,
        displayRes = R.string.button_text_toggle_search_media,
    ),
    SHOWS(
        iconRes = R.drawable.ic_shows_off,
        displayRes = R.string.button_text_shows,
    ),
    MOVIES(
        iconRes = R.drawable.ic_movies_off,
        displayRes = R.string.button_text_movies,
    ),
    PEOPLE(
        iconRes = R.drawable.ic_person_trakt,
        displayRes = R.string.button_label_toggle_search_people,
    ),
}
