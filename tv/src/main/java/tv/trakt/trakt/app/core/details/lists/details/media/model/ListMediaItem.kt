package tv.trakt.trakt.app.core.details.lists.details.media.model

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.app.core.lists.model.ListSortValues
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.ratings.UserRating
import java.time.Instant

@Immutable
internal sealed interface ListMediaItem {
    val id: TraktId
    val title: String
    val images: Images?
    val listedAt: Instant?
    val userRating: UserRating?

    data class ShowItem(
        val show: Show,
        override val listedAt: Instant? = null,
        override val userRating: UserRating? = null,
    ) : ListMediaItem {
        override val id = show.ids.trakt
        override val title = show.title
        override val images = show.images
    }

    data class MovieItem(
        val movie: Movie,
        override val listedAt: Instant? = null,
        override val userRating: UserRating? = null,
    ) : ListMediaItem {
        override val id = movie.ids.trakt
        override val title = movie.title
        override val images = movie.images
    }

    val sortValues: ListSortValues
        get() = when (this) {
            is ShowItem -> ListSortValues.of(show, listedAt, userRating)
            is MovieItem -> ListSortValues.of(movie, listedAt, userRating)
        }

    val key: String
        get() = when (this) {
            is ShowItem -> "show-${id.value}-tv"
            is MovieItem -> "movie-${id.value}-tv"
        }
}
