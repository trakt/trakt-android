package tv.trakt.trakt.app.core.details.lists.details.media.model

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId

@Immutable
internal sealed interface ListMediaItem {
    val id: TraktId
    val title: String
    val images: Images?

    data class ShowItem(
        val show: Show,
    ) : ListMediaItem {
        override val id = show.ids.trakt
        override val title = show.title
        override val images = show.images
    }

    data class MovieItem(
        val movie: Movie,
    ) : ListMediaItem {
        override val id = movie.ids.trakt
        override val title = movie.title
        override val images = movie.images
    }

    val key: String
        get() = when (this) {
            is ShowItem -> "show-${id.value}-tv"
            is MovieItem -> "movie-${id.value}-tv"
        }
}
