package tv.trakt.trakt.common.model.mixtape

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.data.dto.MixtapeDto
import tv.trakt.trakt.common.data.dto.MixtapeMediaDto
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.networking.MovieDto
import tv.trakt.trakt.common.networking.ShowDto

@Immutable
sealed class MixtapeItem {
    @Immutable
    data class MovieItem(
        val movie: Movie,
    ) : MixtapeItem()

    @Immutable
    data class ShowItem(
        val show: Show,
    ) : MixtapeItem()

    val type: MediaType
        get() = when (this) {
            is MovieItem -> MediaType.Movie
            is ShowItem -> MediaType.Show
        }

    val id: TraktId
        get() = when (this) {
            is MovieItem -> movie.ids.trakt
            is ShowItem -> show.ids.trakt
        }

    val key: String
        get() = "${id.value}-${type.value}"

    val title: String
        get() = when (this) {
            is MovieItem -> movie.title
            is ShowItem -> show.title
        }

    val year: Int?
        get() = when (this) {
            is MovieItem -> movie.year
            is ShowItem -> show.year
        }

    val images: Images?
        get() = when (this) {
            is MovieItem -> movie.images
            is ShowItem -> show.images
        }

    companion object {
        fun fromDto(dto: MixtapeMediaDto): MixtapeItem? = of(dto.movie, dto.show)

        fun fromDto(dto: MixtapeDto.Fit): MixtapeItem? = of(dto.movie, dto.show)

        fun fromDto(dto: MixtapeDto.Suggestion): MixtapeItem? = of(dto.movie, dto.show)

        private fun of(
            movie: MovieDto?,
            show: ShowDto?,
        ): MixtapeItem? {
            if (movie != null) return MovieItem(Movie.fromDto(movie))
            if (show != null) return ShowItem(Show.fromDto(show))
            return null
        }
    }
}
