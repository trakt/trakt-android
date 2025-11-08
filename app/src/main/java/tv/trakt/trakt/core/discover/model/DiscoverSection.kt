package tv.trakt.trakt.core.discover.model

import androidx.annotation.StringRes
import tv.trakt.trakt.core.main.model.MediaMode
import tv.trakt.trakt.core.main.model.MediaMode.MEDIA
import tv.trakt.trakt.core.main.model.MediaMode.MOVIES
import tv.trakt.trakt.core.main.model.MediaMode.SHOWS
import tv.trakt.trakt.resources.R

internal enum class DiscoverSection {
    TRENDING,
    POPULAR,
    ANTICIPATED,
    RECOMMENDED,
    ;

    @StringRes
    fun getTitle(mode: MediaMode): Int {
        return when (this) {
            TRENDING -> when (mode) {
                MEDIA -> R.string.list_title_trending
                SHOWS -> R.string.list_title_trending_shows
                MOVIES -> R.string.list_title_trending_movies
            }
            POPULAR -> when (mode) {
                MEDIA -> R.string.list_title_most_popular
                SHOWS -> R.string.list_title_popular_shows
                MOVIES -> R.string.list_title_popular_movies
            }
            ANTICIPATED -> when (mode) {
                MEDIA -> R.string.list_title_most_anticipated
                SHOWS -> R.string.list_title_anticipated_shows
                MOVIES -> R.string.list_title_anticipated_movies
            }
            RECOMMENDED -> when (mode) {
                MEDIA -> R.string.list_title_recommended
                SHOWS -> R.string.list_title_recommended_shows
                MOVIES -> R.string.list_title_recommended_movies
            }
        }
    }
}
