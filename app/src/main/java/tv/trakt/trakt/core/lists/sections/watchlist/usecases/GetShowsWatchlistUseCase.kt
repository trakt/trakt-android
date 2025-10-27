package tv.trakt.trakt.core.lists.sections.watchlist.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.core.lists.sections.watchlist.model.WatchlistItem
import tv.trakt.trakt.core.user.usecases.lists.LoadUserWatchlistUseCase

internal class GetShowsWatchlistUseCase(
    private val loadUserWatchlistUseCase: LoadUserWatchlistUseCase,
) {
    suspend fun getLocalWatchlist(limit: Int? = null): ImmutableList<WatchlistItem> {
        return loadUserWatchlistUseCase
            .loadLocalShows()
            .sortedByDescending { it.listedAt }
            .take(limit ?: Int.MAX_VALUE)
            .toImmutableList()
    }

    suspend fun getWatchlist(limit: Int? = null): ImmutableList<WatchlistItem> {
        return loadUserWatchlistUseCase.loadWatchlist()
            .filterIsInstance<WatchlistItem.ShowItem>()
            .sortedByDescending { it.listedAt }
            .take(limit ?: Int.MAX_VALUE)
            .toImmutableList()
    }
}
