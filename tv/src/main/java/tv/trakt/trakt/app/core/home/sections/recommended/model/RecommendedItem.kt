package tv.trakt.trakt.app.core.home.sections.recommended.model

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId

@Immutable
internal sealed class RecommendedItem {
    @Immutable
    internal data class ShowItem(
        val show: Show,
    ) : RecommendedItem()

    @Immutable
    internal data class MovieItem(
        val movie: Movie,
    ) : RecommendedItem()

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
            is ShowItem -> "${show.ids.trakt.value}-show-recommended"
            is MovieItem -> "${movie.ids.trakt.value}-movie-recommended"
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
