package tv.trakt.trakt.tv.core.home.sections.movies.availablenow.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.extensions.toZonedDateTime
import tv.trakt.trakt.tv.core.home.sections.movies.availablenow.model.WatchlistMovie
import tv.trakt.trakt.tv.core.movies.data.local.MovieLocalDataSource
import tv.trakt.trakt.tv.core.movies.model.Movie
import tv.trakt.trakt.tv.core.movies.model.fromDto
import tv.trakt.trakt.tv.core.sync.data.remote.movies.MoviesSyncRemoteDataSource
import tv.trakt.trakt.tv.helpers.extensions.asyncMap
import java.time.LocalDate

internal class GetAvailableNowMoviesUseCase(
    private val remoteSyncSource: MoviesSyncRemoteDataSource,
    private val localMovieSource: MovieLocalDataSource,
) {
    suspend fun getMovies(): ImmutableList<WatchlistMovie> {
        val nowDay = LocalDate.now().toString()
        val response = remoteSyncSource.getWatchlist(
            limit = 30,
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
