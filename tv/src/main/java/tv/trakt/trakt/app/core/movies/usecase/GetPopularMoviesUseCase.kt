package tv.trakt.trakt.app.core.movies.usecase

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.app.core.movies.MoviesConfig.MOVIES_SECTION_LIMIT
import tv.trakt.trakt.app.core.movies.data.remote.MoviesRemoteDataSource
import tv.trakt.trakt.common.core.movies.data.local.MovieLocalDataSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.nowLocalDay
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.fromDto
import java.time.Year

internal class GetPopularMoviesUseCase(
    private val remoteSource: MoviesRemoteDataSource,
    private val localSource: MovieLocalDataSource,
) {
    suspend fun getPopularMovies(
        limit: Int = MOVIES_SECTION_LIMIT,
        page: Int = 1,
    ): ImmutableList<Movie> {
        return remoteSource.getPopularMovies(
            limit = limit,
            page = page,
            years = getYearsRange(),
        )
            .asyncMap {
                Movie.fromDto(it)
            }
            .toImmutableList()
            .also { movies ->
                localSource.upsertMovies(movies)
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
