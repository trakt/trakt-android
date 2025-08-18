package tv.trakt.trakt.app.core.home.sections.movies.comingsoon.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.app.core.home.HomeConfig.HOME_SECTION_LIMIT
import tv.trakt.trakt.app.core.home.sections.movies.availablenow.model.WatchlistMovie
import tv.trakt.trakt.app.core.movies.data.local.MovieLocalDataSource
import tv.trakt.trakt.app.core.sync.data.remote.movies.MoviesSyncRemoteDataSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.toZonedDateTime
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.fromDto
import java.time.LocalDate

internal class GetComingSoonMoviesUseCase(
    private val remoteSyncSource: MoviesSyncRemoteDataSource,
    private val localMovieSource: MovieLocalDataSource,
) {
    suspend fun getMovies(
        limit: Int = HOME_SECTION_LIMIT,
        page: Int = 1,
    ): ImmutableList<WatchlistMovie> {
        val nowDay = LocalDate.now()
        val dayLimit = nowDay.plusMonths(12)

        val response = remoteSyncSource.getWatchlist(
            limit = limit,
            page = page,
            sort = "released",
            extended = "full,cloud9,colors",
        ).asyncMap {
            WatchlistMovie(
                movie = Movie.fromDto(it.movie),
                listedAt = it.listedAt.toZonedDateTime(),
                rank = it.rank,
            )
        }.filter {
            val released = it.movie.released
            released != null &&
                released > nowDay &&
                released <= dayLimit
        }.sortedBy {
            it.movie.released
        }

        return response
            .toImmutableList()
            .also {
                val movies = response.asyncMap { it.movie }
                localMovieSource.upsertMovies(movies)
            }
    }
}
