package tv.trakt.trakt.core.discover.sections.popular.usecases.movies

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.core.movies.data.local.MovieLocalDataSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.nowLocalDay
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.core.discover.DiscoverConfig
import tv.trakt.trakt.core.discover.model.DiscoverItem
import tv.trakt.trakt.core.discover.sections.popular.data.local.movies.PopularMoviesLocalDataSource
import tv.trakt.trakt.core.discover.sections.popular.usecases.GetPopularMoviesUseCase
import tv.trakt.trakt.core.movies.data.remote.MoviesRemoteDataSource
import java.time.Year

internal class DefaultGetPopularMoviesUseCase(
    private val remoteSource: MoviesRemoteDataSource,
    private val localPopularSource: PopularMoviesLocalDataSource,
    private val localMovieSource: MovieLocalDataSource,
) : GetPopularMoviesUseCase {
    override suspend fun getLocalMovies(): ImmutableList<DiscoverItem.MovieItem> {
        return localPopularSource.getMovies()
            .toImmutableList()
            .also {
                localMovieSource.upsertMovies(
                    it.asyncMap { item -> item.movie },
                )
            }
    }

    override suspend fun getMovies(
        limit: Int,
        page: Int,
        skipLocal: Boolean,
    ): ImmutableList<DiscoverItem.MovieItem> {
        return remoteSource.getPopular(
            page = page,
            limit = limit,
            years = getYearsRange().toString(),
        )
            .mapIndexed { index, movieDto ->
                DiscoverItem.MovieItem(
                    movie = Movie.fromDto(movieDto),
                    count = index + 1, // Use ranking position as count
                )
            }
            .toImmutableList()
            .also { movies ->
                if (!skipLocal) {
                    localPopularSource.addMovies(
                        movies = movies.take(DiscoverConfig.DEFAULT_SECTION_LIMIT),
                        addedAt = nowUtcInstant(),
                    )
                }

                localMovieSource.upsertMovies(
                    movies.asyncMap { item -> item.movie },
                )
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
