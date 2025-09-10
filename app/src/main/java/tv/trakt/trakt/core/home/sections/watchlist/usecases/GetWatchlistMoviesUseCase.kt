package tv.trakt.trakt.core.home.sections.watchlist.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.toInstant
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.core.home.HomeConfig.HOME_WATCHLIST_LIMIT
import tv.trakt.trakt.core.home.sections.watchlist.data.local.HomeWatchlistLocalDataSource
import tv.trakt.trakt.core.home.sections.watchlist.model.WatchlistMovie
import tv.trakt.trakt.core.sync.data.remote.movies.MoviesSyncRemoteDataSource
import java.time.LocalDate

internal class GetWatchlistMoviesUseCase(
    private val remoteSyncSource: MoviesSyncRemoteDataSource,
    private val localDataSource: HomeWatchlistLocalDataSource,
) {
    suspend fun getLocalWatchlist(): ImmutableList<WatchlistMovie> {
        return localDataSource.getItems()
            .sortedByDescending {
                it.movie.released
            }
            .toImmutableList()
    }

    suspend fun getWatchlist(
        limit: Int = HOME_WATCHLIST_LIMIT,
        page: Int = 1,
    ): ImmutableList<WatchlistMovie> {
        val nowDay = LocalDate.now().toString()
        val response = remoteSyncSource.getWatchlist(
            page = page,
            limit = limit,
            sort = "released",
            extended = "full,cloud9",
        ).filter {
            !it.movie.released.isNullOrBlank() && it.movie.released!! <= nowDay
        }.asyncMap {
            WatchlistMovie(
                movie = Movie.fromDto(it.movie),
                listedAt = it.listedAt.toInstant(),
                rank = it.rank,
            )
        }.sortedByDescending {
            it.movie.released
        }

        return response
            .toImmutableList()
            .also {
                localDataSource.addItems(items = it)
            }
    }
}
