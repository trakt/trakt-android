package tv.trakt.trakt.common.model.livingposter

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.data.dto.LivingPosterIndexDto
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.TraktId

private const val SUPPORTED_VERSION = 1

@Immutable
data class LivingPosterIndex(
    val movies: Set<TraktId>,
    val shows: Set<TraktId>,
) {
    fun contains(
        type: MediaType,
        id: TraktId,
    ): Boolean {
        return when (type) {
            MediaType.Movie -> id in movies
            MediaType.Show -> id in shows
            MediaType.Season, MediaType.Episode -> false
        }
    }

    companion object {
        fun fromDto(dto: LivingPosterIndexDto): LivingPosterIndex? {
            if (dto.v != SUPPORTED_VERSION) return null

            return LivingPosterIndex(
                movies = dto.movies.orEmpty().map(::TraktId).toSet(),
                shows = dto.shows.orEmpty().map(::TraktId).toSet(),
            )
        }
    }
}
