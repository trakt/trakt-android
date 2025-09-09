package tv.trakt.trakt.core.movies.sections.anticipated.usecase

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.core.movies.data.remote.MoviesRemoteDataSource
import tv.trakt.trakt.core.movies.model.WatchersMovie
import tv.trakt.trakt.core.movies.sections.anticipated.data.local.AnticipatedMoviesLocalDataSource
import java.time.temporal.ChronoUnit.DAYS

internal class GetAnticipatedMoviesUseCase(
    private val remoteSource: MoviesRemoteDataSource,
    private val localAnticipatedSource: AnticipatedMoviesLocalDataSource,
) {
    suspend fun getLocalMovies(): ImmutableList<WatchersMovie> {
        return localAnticipatedSource.getMovies()
            .sortedByDescending { it.watchers }
            .toImmutableList()
    }

    suspend fun getMovies(): ImmutableList<WatchersMovie> {
        return remoteSource.getAnticipated(
            limit = 20,
            endDate = nowUtcInstant().plus(365, DAYS).truncatedTo(DAYS),
        )
            .asyncMap {
                WatchersMovie(
                    watchers = it.listCount,
                    movie = Movie.fromDto(it.movie),
                )
            }
            .toImmutableList()
            .also { movies ->
                localAnticipatedSource.addMovies(
                    movies = movies,
                    addedAt = nowUtcInstant(),
                )
            }
    }
}
