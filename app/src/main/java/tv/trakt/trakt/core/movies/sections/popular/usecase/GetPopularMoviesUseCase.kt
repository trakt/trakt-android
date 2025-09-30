package tv.trakt.trakt.core.movies.sections.popular.usecase

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.core.movies.data.local.MovieLocalDataSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.nowLocalDay
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.core.movies.data.remote.MoviesRemoteDataSource
import tv.trakt.trakt.core.movies.sections.popular.data.local.PopularMoviesLocalDataSource
import java.time.Instant
import java.time.Year

private const val DEFAULT_LIMIT = 24
internal const val DEFAULT_ALL_LIMIT = 102

internal class GetPopularMoviesUseCase(
    private val remoteSource: MoviesRemoteDataSource,
    private val localPopularSource: PopularMoviesLocalDataSource,
    private val localMovieSource: MovieLocalDataSource,
) {
    suspend fun getLocalMovies(): ImmutableList<Movie> {
        return localPopularSource.getMovies()
            .toImmutableList()
            .also {
                localMovieSource.upsertMovies(it)
            }
    }

    suspend fun getMovies(
        limit: Int = DEFAULT_LIMIT,
        page: Int = 1,
        skipLocal: Boolean = false,
    ): ImmutableList<Movie> {
        return remoteSource.getPopular(
            limit = limit,
            years = getYearsRange(),
            page = page,
        )
            .asyncMap {
                Movie.fromDto(it)
            }
            .toImmutableList()
            .also { movies ->
                if (!skipLocal) {
                    localPopularSource.addMovies(
                        movies = movies.take(DEFAULT_LIMIT),
                        addedAt = Instant.now(),
                    )

                    localMovieSource.upsertMovies(movies)
                }
            }
    }

    private fun getYearsRange(): Int {
        val currentYear = Year.now().value
        val currentMonth = nowLocalDay().monthValue
        return if (currentMonth <= 3) {
            currentYear - 1
        } else {
            currentYear
        }
    }
}
