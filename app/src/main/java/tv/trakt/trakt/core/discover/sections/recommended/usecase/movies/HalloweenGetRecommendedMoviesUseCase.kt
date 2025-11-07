package tv.trakt.trakt.core.discover.sections.recommended.usecase.movies

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.core.movies.data.local.MovieLocalDataSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.core.discover.DiscoverConfig.DEFAULT_SECTION_LIMIT
import tv.trakt.trakt.core.discover.model.DiscoverItem
import tv.trakt.trakt.core.discover.sections.recommended.data.local.movies.RecommendedMoviesLocalDataSource
import tv.trakt.trakt.core.discover.sections.recommended.usecase.GetRecommendedMoviesUseCase
import tv.trakt.trakt.core.movies.data.remote.MoviesRemoteDataSource
import java.time.Instant

internal class HalloweenGetRecommendedMoviesUseCase(
    private val remoteSource: MoviesRemoteDataSource,
    private val localRecommendedSource: RecommendedMoviesLocalDataSource,
    private val localMovieSource: MovieLocalDataSource,
) : GetRecommendedMoviesUseCase {
    override suspend fun getLocalMovies(): ImmutableList<DiscoverItem.MovieItem> {
        return localRecommendedSource.getMovies()
            .toImmutableList()
            .also {
                localMovieSource.upsertMovies(
                    it.asyncMap { item -> item.movie },
                )
            }
    }

    override suspend fun getMovies(
        limit: Int,
        skipLocal: Boolean,
    ): ImmutableList<DiscoverItem.MovieItem> {
        return remoteSource.getRecommended(
            limit = limit,
            subgenres = listOf("halloween"),
        )
            .asyncMap {
                DiscoverItem.MovieItem(
                    movie = Movie.fromDto(it),
                    count = 0, // No ranking for recommended movies
                )
            }
            .toImmutableList()
            .also { movies ->
                if (!skipLocal) {
                    localRecommendedSource.addMovies(
                        movies = movies.take(DEFAULT_SECTION_LIMIT),
                        addedAt = Instant.now(),
                    )
                }

                localMovieSource.upsertMovies(
                    movies.asyncMap { item -> item.movie },
                )
            }
    }
}
