package tv.trakt.trakt.core.discover.sections.trending.usecases.movies

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.core.movies.data.local.MovieLocalDataSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.core.discover.DiscoverConfig.DEFAULT_SECTION_LIMIT
import tv.trakt.trakt.core.discover.model.DiscoverItem
import tv.trakt.trakt.core.discover.sections.trending.data.local.movies.TrendingMoviesLocalDataSource
import tv.trakt.trakt.core.discover.sections.trending.usecases.GetTrendingMoviesUseCase
import tv.trakt.trakt.core.movies.data.remote.MoviesRemoteDataSource

internal class HalloweenGetTrendingMoviesUseCase(
    private val remoteSource: MoviesRemoteDataSource,
    private val localTrendingSource: TrendingMoviesLocalDataSource,
    private val localMovieSource: MovieLocalDataSource,
) : GetTrendingMoviesUseCase {
    override suspend fun getLocalMovies(): ImmutableList<DiscoverItem.MovieItem> {
        return localTrendingSource.getMovies()
            .sortedByDescending { it.count }
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
        return remoteSource.getTrending(
            page = page,
            limit = limit,
            subgenres = listOf("halloween"),
        )
            .asyncMap {
                DiscoverItem.MovieItem(
                    movie = Movie.fromDto(it.movie),
                    count = it.watchers,
                )
            }
            .toImmutableList()
            .also { movies ->
                if (!skipLocal) {
                    localTrendingSource.addMovies(
                        movies = movies.take(DEFAULT_SECTION_LIMIT),
                        addedAt = nowUtcInstant(),
                    )
                }

                localMovieSource.upsertMovies(
                    movies.asyncMap { item -> item.movie },
                )
            }
    }
}
