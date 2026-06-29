package tv.trakt.trakt.common.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable
import tv.trakt.trakt.resources.R

@Immutable
@Serializable
enum class MediaMode(
    @param:StringRes val displayRes: Int,
    @param:DrawableRes val offIcon: Int,
    @param:DrawableRes val onIcon: Int,
) {
    Media(
        R.string.button_text_toggle_search_media,
        R.drawable.ic_shows_movies,
        R.drawable.ic_shows_movies_on,
    ),
    Shows(
        R.string.button_text_shows,
        R.drawable.ic_shows_off,
        R.drawable.ic_shows_on,
    ),
    Movies(
        R.string.button_text_movies,
        R.drawable.ic_movies_off,
        R.drawable.ic_movies_on,
    ),
    ;

    val isMediaOrShows: Boolean
        get() = this == Media || this == Shows

    val isMediaOrMovies: Boolean
        get() = this == Media || this == Movies
}
