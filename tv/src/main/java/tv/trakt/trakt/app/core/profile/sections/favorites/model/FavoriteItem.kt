package tv.trakt.trakt.app.core.profile.sections.favorites.model

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import java.time.Instant

@Immutable
internal sealed class FavoriteItem {
    @Immutable
    internal data class ShowItem(
        val show: Show,
        val rank: Int,
        val listedAt: Instant,
    ) : FavoriteItem()

    @Immutable
    internal data class MovieItem(
        val movie: Movie,
        val rank: Int,
        val listedAt: Instant,
    ) : FavoriteItem()

    val id: TraktId
        get() = when (this) {
            is ShowItem -> show.ids.trakt
            is MovieItem -> movie.ids.trakt
        }

    val mediaType: MediaType
        get() = when (this) {
            is ShowItem -> MediaType.Show
            is MovieItem -> MediaType.Movie
        }

    val airedEpisodes: Int?
        get() = when (this) {
            is ShowItem -> show.airedEpisodes
            is MovieItem -> null
        }

    val key: String
        get() = when (this) {
            is ShowItem -> "${show.ids.trakt.value}-show-favorite"
            is MovieItem -> "${movie.ids.trakt.value}-movie-favorite"
        }

    val title: String
        get() = when (this) {
            is ShowItem -> show.title
            is MovieItem -> movie.title
        }

    val posterImage: String?
        get() = when (this) {
            is ShowItem -> show.images?.getPosterUrl(Images.Size.MEDIUM)
            is MovieItem -> movie.images?.getPosterUrl(Images.Size.MEDIUM)
        }

    val fullFanartImage: String?
        get() = when (this) {
            is ShowItem -> show.images?.getFanartUrl(Images.Size.FULL)
            is MovieItem -> movie.images?.getFanartUrl(Images.Size.FULL)
        }
}

internal fun interleaveFavorites(
    shows: List<FavoriteItem.ShowItem>,
    movies: List<FavoriteItem.MovieItem>,
): List<FavoriteItem> =
    (0 until maxOf(shows.size, movies.size)).flatMap { index ->
        listOfNotNull(
            shows.getOrNull(index),
            movies.getOrNull(index),
        )
    }
