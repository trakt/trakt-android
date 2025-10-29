package tv.trakt.trakt.core.movies.sections.anticipated.usecase.anticipated

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.core.movies.data.local.MovieLocalDataSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.nowLocal
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.core.movies.data.remote.MoviesRemoteDataSource
import tv.trakt.trakt.core.movies.model.WatchersMovie
import tv.trakt.trakt.core.movies.sections.anticipated.data.local.AnticipatedMoviesLocalDataSource
import tv.trakt.trakt.core.movies.sections.anticipated.usecase.GetAnticipatedMoviesUseCase

private const val DEFAULT_LIMIT = 24

internal class HalloweenGetAnticipatedMoviesUseCase(
    private val remoteSource: MoviesRemoteDataSource,
    private val localAnticipatedSource: AnticipatedMoviesLocalDataSource,
    private val localMovieSource: MovieLocalDataSource,
) : GetAnticipatedMoviesUseCase {
    override suspend fun getLocalMovies(): ImmutableList<WatchersMovie> {
        return localAnticipatedSource.getMovies()
            .sortedByDescending { it.watchers }
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
    ): ImmutableList<WatchersMovie> {
        val now = nowLocal()
        return remoteSource.getAnticipated(
            page = page,
            limit = limit,
            years = "${now.minusYears(3).year}-${now.year}",
            subgenres = listOf("halloween"),
        )
            .asyncMap {
                WatchersMovie(
                    watchers = it.listCount,
                    movie = Movie.fromDto(it.movie),
                )
            }
            .toImmutableList()
            .also { movies ->
                if (!skipLocal) {
                    localAnticipatedSource.addMovies(
                        movies = movies.take(DEFAULT_LIMIT),
                        addedAt = nowUtcInstant(),
                    )
                }

                localMovieSource.upsertMovies(
                    movies.asyncMap { item -> item.movie },
                )
            }
    }
}
