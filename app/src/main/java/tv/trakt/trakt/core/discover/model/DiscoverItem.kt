package tv.trakt.trakt.core.discover.model

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.resources.R

@Immutable
internal sealed class DiscoverItem(
    open val count: Int,
) {
    @Immutable
    internal data class MovieItem(
        val movie: Movie,
        override val count: Int = 0,
    ) : DiscoverItem(count)

    @Immutable
    internal data class ShowItem(
        val show: Show,
        override val count: Int = 0,
    ) : DiscoverItem(count)

    val type: MediaType
        get() = when (this) {
            is ShowItem -> MediaType.SHOW
            is MovieItem -> MediaType.MOVIE
        }

    val id: TraktId
        get() = when (this) {
            is ShowItem -> show.ids.trakt
            is MovieItem -> movie.ids.trakt
        }

    val key: String
        get() = when (this) {
            is ShowItem -> "${id.value}-show"
            is MovieItem -> "${id.value}-movie"
        }

    val title: String
        get() = when (this) {
            is ShowItem -> show.title
            is MovieItem -> movie.title
        }

    val icon: Int
        get() = when (this) {
            is ShowItem -> R.drawable.ic_shows_off
            is MovieItem -> R.drawable.ic_movies_off
        }

    val images: Images?
        get() = when (this) {
            is ShowItem -> show.images
            is MovieItem -> movie.images
        }
}
