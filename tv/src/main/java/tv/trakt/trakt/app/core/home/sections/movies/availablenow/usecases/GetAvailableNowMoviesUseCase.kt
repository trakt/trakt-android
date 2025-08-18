package tv.trakt.trakt.app.core.home.sections.movies.availablenow.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.app.core.home.sections.movies.availablenow.model.WatchlistMovie
import tv.trakt.trakt.app.core.movies.MoviesConfig.MOVIES_SECTION_LIMIT
import tv.trakt.trakt.app.core.movies.data.local.MovieLocalDataSource
import tv.trakt.trakt.app.core.sync.data.remote.movies.MoviesSyncRemoteDataSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.toZonedDateTime
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.fromDto
import java.time.LocalDate

internal class GetAvailableNowMoviesUseCase(
    private val remoteSyncSource: MoviesSyncRemoteDataSource,
    private val localMovieSource: MovieLocalDataSource,
) {
    suspend fun getMovies(
        limit: Int = MOVIES_SECTION_LIMIT,
        page: Int = 1,
    ): ImmutableList<WatchlistMovie> {
        val nowDay = LocalDate.now().toString()
        val response = remoteSyncSource.getWatchlist(
            page = page,
            limit = limit,
            extended = "full,cloud9,colors",
        ).filter {
            !it.movie.released.isNullOrBlank() && it.movie.released!! <= nowDay
        }.asyncMap {
            WatchlistMovie(
                movie = Movie.fromDto(it.movie),
                listedAt = it.listedAt.toZonedDateTime(),
                rank = it.rank,
            )
        }.sortedByDescending { it.movie.released }

        return response
            .toImmutableList()
            .also {
                val movies = response.asyncMap { it.movie }
                localMovieSource.upsertMovies(movies)
            }
    }
}
