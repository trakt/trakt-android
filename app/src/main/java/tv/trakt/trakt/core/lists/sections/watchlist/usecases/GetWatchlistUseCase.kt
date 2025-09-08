package tv.trakt.trakt.core.lists.sections.watchlist.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.core.lists.ListsConfig.LISTS_SECTION_LIMIT
import tv.trakt.trakt.core.lists.sections.watchlist.model.WatchlistItem
import tv.trakt.trakt.core.profile.data.remote.UserRemoteDataSource

internal class GetWatchlistUseCase(
    private val remoteSource: UserRemoteDataSource,
) {
//    suspend fun getLocalWatchlist(): ImmutableList<WatchlistMovie> {
//        return localDataSource.getItems()
//            .sortedByDescending {
//                it.movie.released
//            }
//            .toImmutableList()
//    }

    suspend fun getWatchlist(
        limit: Int = LISTS_SECTION_LIMIT,
        page: Int = 1,
    ): ImmutableList<WatchlistItem> {
        val response = remoteSource.getWatchlist(
            page = page,
            limit = limit,
            sort = "added",
            extended = "full,cloud9",
        ).asyncMap {
            when {
                it.movie != null -> {
                    WatchlistItem.MovieItem(
                        movie = Movie.fromDto(it.movie!!),
                    )
                }
                it.show != null -> {
                    WatchlistItem.ShowItem(
                        show = Show.fromDto(it.show!!),
                    )
                }
                else -> {
                    throw IllegalStateException("Watchlist item unknown type!")
                }
            }
        }
        return response.toImmutableList()
    }
}
