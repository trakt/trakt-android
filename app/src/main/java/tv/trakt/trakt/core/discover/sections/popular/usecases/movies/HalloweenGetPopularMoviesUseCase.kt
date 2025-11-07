package tv.trakt.trakt.core.discover.sections.popular.usecases.movies

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.core.movies.data.local.MovieLocalDataSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.nowLocal
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.core.discover.DiscoverConfig.DEFAULT_SECTION_LIMIT
import tv.trakt.trakt.core.discover.model.DiscoverItem
import tv.trakt.trakt.core.discover.sections.popular.data.local.movies.PopularMoviesLocalDataSource
import tv.trakt.trakt.core.discover.sections.popular.usecases.GetPopularMoviesUseCase
import tv.trakt.trakt.core.movies.data.remote.MoviesRemoteDataSource
import java.time.Instant

internal class HalloweenGetPopularMoviesUseCase(
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
            subgenres = listOf("halloween"),
            years = "1990-${nowLocal().year}",
        )
            .asyncMap {
                DiscoverItem.MovieItem(
                    movie = Movie.fromDto(it),
                    count = 0,
                )
            }
            .toImmutableList()
            .also { movies ->
                if (!skipLocal) {
                    localPopularSource.addMovies(
                        movies = movies.take(DEFAULT_SECTION_LIMIT),
                        addedAt = Instant.now(),
                    )

                    localMovieSource.upsertMovies(
                        movies.asyncMap { item -> item.movie },
                    )
                }
            }
    }
}
