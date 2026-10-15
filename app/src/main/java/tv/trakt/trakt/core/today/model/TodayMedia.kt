package tv.trakt.trakt.core.today.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.model.Ids
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.common.model.MediaGenre
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Rating
import tv.trakt.trakt.common.model.Show

@Immutable
internal sealed interface TodayMedia {
    val ids: Ids
    val type: MediaType
    val title: String
    val images: Images?
    val overview: String?
    val year: Int?
    val genres: ImmutableList<MediaGenre>
    val rating: Rating

    val key: String
        get() = "${type.value}-${ids.trakt.value}"

    @Immutable
    data class MovieMedia(
        val movie: Movie,
    ) : TodayMedia {
        override val ids = movie.ids
        override val type = MediaType.Movie
        override val title = movie.title
        override val images = movie.images
        override val overview = movie.overview
        override val year = movie.year
        override val genres = movie.genres
        override val rating = movie.rating
    }

    @Immutable
    data class ShowMedia(
        val show: Show,
    ) : TodayMedia {
        override val ids = show.ids
        override val type = MediaType.Show
        override val title = show.title
        override val images = show.images
        override val overview = show.overview
        override val year = show.year
        override val genres = show.genres
        override val rating = show.rating
    }
}
