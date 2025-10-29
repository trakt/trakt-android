package tv.trakt.trakt.core.movies.sections.popular.usecase.popular

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.core.movies.data.local.MovieLocalDataSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.nowLocal
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.core.movies.data.remote.MoviesRemoteDataSource
import tv.trakt.trakt.core.movies.sections.popular.data.local.PopularMoviesLocalDataSource
import tv.trakt.trakt.core.movies.sections.popular.usecase.GetPopularMoviesUseCase
import java.time.Instant

private const val DEFAULT_LIMIT = 24

internal class HalloweenGetPopularMoviesUseCase(
    private val remoteSource: MoviesRemoteDataSource,
    private val localPopularSource: PopularMoviesLocalDataSource,
    private val localMovieSource: MovieLocalDataSource,
) : GetPopularMoviesUseCase {
    override suspend fun getLocalMovies(): ImmutableList<Movie> {
        return localPopularSource.getMovies()
            .toImmutableList()
            .also {
                localMovieSource.upsertMovies(it)
            }
    }

    override suspend fun getMovies(
        limit: Int,
        page: Int,
        skipLocal: Boolean,
    ): ImmutableList<Movie> {
        return remoteSource.getPopular(
            page = page,
            limit = limit,
            subgenres = listOf("halloween"),
            years = "1990-${nowLocal().year}",
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
}
