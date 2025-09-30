package tv.trakt.trakt.app.core.movies.usecase

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.app.core.movies.MoviesConfig.MOVIES_SECTION_LIMIT
import tv.trakt.trakt.app.core.movies.data.remote.MoviesRemoteDataSource
import tv.trakt.trakt.app.core.movies.model.AnticipatedMovie
import tv.trakt.trakt.common.core.movies.data.local.MovieLocalDataSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.fromDto
import java.time.temporal.ChronoUnit.DAYS

internal class GetAnticipatedMoviesUseCase(
    private val remoteSource: MoviesRemoteDataSource,
    private val localSource: MovieLocalDataSource,
) {
    suspend fun getAnticipatedMovies(
        limit: Int = MOVIES_SECTION_LIMIT,
        page: Int = 1,
    ): ImmutableList<AnticipatedMovie> {
        return remoteSource.getAnticipatedMovies(
            limit = limit,
            page = page,
            endDate = nowUtcInstant().plus(365, DAYS).truncatedTo(DAYS),
        )
            .asyncMap {
                AnticipatedMovie(
                    listCount = it.listCount,
                    movie = Movie.fromDto(it.movie),
                )
            }
            .toImmutableList()
            .also { movies ->
                localSource.upsertMovies(movies.map { it.movie })
            }
    }
}
