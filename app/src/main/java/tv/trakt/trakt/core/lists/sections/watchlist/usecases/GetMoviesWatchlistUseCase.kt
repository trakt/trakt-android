package tv.trakt.trakt.core.lists.sections.watchlist.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.core.lists.ListsConfig.LISTS_SECTION_LIMIT
import tv.trakt.trakt.core.lists.sections.watchlist.model.WatchlistItem
import tv.trakt.trakt.core.sync.data.remote.movies.MoviesSyncRemoteDataSource

internal class GetMoviesWatchlistUseCase(
    private val remoteSyncSource: MoviesSyncRemoteDataSource,
) {
    suspend fun getWatchlist(
        limit: Int = LISTS_SECTION_LIMIT,
        page: Int = 1,
    ): ImmutableList<WatchlistItem> {
        val response = remoteSyncSource.getWatchlist(
            page = page,
            limit = limit,
            sort = "added",
            extended = "full,cloud9",
        ).asyncMap {
            WatchlistItem.MovieItem(
                movie = Movie.fromDto(it.movie),
            )
        }

        return response
            .toImmutableList()
    }
}
