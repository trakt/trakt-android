package tv.trakt.trakt.core.main.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable
import tv.trakt.trakt.resources.R

@Immutable
@Serializable
enum class MediaMode(
    @param:StringRes val label: Int,
    @param:DrawableRes val offIcon: Int,
    @param:DrawableRes val onIcon: Int,
) {
    MEDIA(
        R.string.button_text_toggle_search_media,
        R.drawable.ic_shows_movies,
        R.drawable.ic_shows_movies_on,
    ),
    SHOWS(
        R.string.button_text_shows,
        R.drawable.ic_shows_off,
        R.drawable.ic_shows_on,
    ),
    MOVIES(
        R.string.button_text_movies,
        R.drawable.ic_movies_off,
        R.drawable.ic_movies_on,
    ),
    ;

    val isMediaOrShows: Boolean
        get() = this == MEDIA || this == SHOWS

    val isMediaOrMovies: Boolean
        get() = this == MEDIA || this == MOVIES
}
