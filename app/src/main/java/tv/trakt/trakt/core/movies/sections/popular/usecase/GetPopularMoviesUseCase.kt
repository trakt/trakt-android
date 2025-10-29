package tv.trakt.trakt.core.movies.sections.popular.usecase

import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.model.Movie

private const val DEFAULT_LIMIT = 24
internal const val DEFAULT_ALL_LIMIT = 102

internal interface GetPopularMoviesUseCase {
    suspend fun getLocalMovies(): ImmutableList<Movie>

    suspend fun getMovies(
        limit: Int = DEFAULT_LIMIT,
        page: Int = 1,
        skipLocal: Boolean = false,
    ): ImmutableList<Movie>
}
