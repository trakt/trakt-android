package tv.trakt.trakt.core.lists.model

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import java.time.Instant

@Immutable
internal sealed class PersonalListItem(
    open val listedAt: Instant,
    open val loading: Boolean,
) {
    @Immutable
    internal data class MovieItem(
        val movie: Movie,
        override val listedAt: Instant,
        override val loading: Boolean = false,
    ) : PersonalListItem(listedAt, loading)

    @Immutable
    internal data class ShowItem(
        val show: Show,
        override val listedAt: Instant,
        override val loading: Boolean = false,
    ) : PersonalListItem(listedAt, loading)

    val id: TraktId
        get() = when (this) {
            is ShowItem -> show.ids.trakt
            is MovieItem -> movie.ids.trakt
        }

    val type: MediaType
        get() = when (this) {
            is ShowItem -> MediaType.SHOW
            is MovieItem -> MediaType.MOVIE
        }

    val key: String
        get() = "$id-$type"

    val images: Images?
        get() = when (this) {
            is ShowItem -> show.images
            is MovieItem -> movie.images
        }
}
