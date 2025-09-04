package tv.trakt.trakt.core.search.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import tv.trakt.trakt.resources.R

enum class SearchFilter(
    @param:DrawableRes val iconRes: Int,
    @param:StringRes val displayRes: Int,
    @param:StringRes val placeholderRes: Int,
) {
    MEDIA(
        iconRes = R.drawable.ic_shows_movies,
        displayRes = R.string.button_text_toggle_search_media,
        placeholderRes = R.string.input_placeholder_search,
    ),
    SHOWS(
        iconRes = R.drawable.ic_shows_off,
        displayRes = R.string.button_text_shows,
        placeholderRes = R.string.input_placeholder_search_shows,
    ),
    MOVIES(
        iconRes = R.drawable.ic_movies_off,
        displayRes = R.string.button_text_movies,
        placeholderRes = R.string.input_placeholder_search_movies,
    ),
    PEOPLE(
        iconRes = R.drawable.ic_person_trakt,
        displayRes = R.string.button_text_toggle_search_people,
        placeholderRes = R.string.input_placeholder_search_people,
    ),
}
