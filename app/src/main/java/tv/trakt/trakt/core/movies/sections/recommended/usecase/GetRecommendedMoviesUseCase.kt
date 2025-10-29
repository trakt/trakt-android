package tv.trakt.trakt.core.movies.sections.recommended.usecase

import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.model.Movie

private const val DEFAULT_LIMIT = 24
internal const val DEFAULT_ALL_LIMIT = 200

internal interface GetRecommendedMoviesUseCase {
    suspend fun getLocalMovies(): ImmutableList<Movie>

    suspend fun getMovies(
        limit: Int = DEFAULT_LIMIT,
        skipLocal: Boolean = false,
    ): ImmutableList<Movie>
}
