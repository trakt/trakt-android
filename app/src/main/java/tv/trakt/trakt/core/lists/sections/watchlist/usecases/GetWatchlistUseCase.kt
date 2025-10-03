package tv.trakt.trakt.core.lists.sections.watchlist.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.core.lists.sections.watchlist.model.WatchlistItem
import tv.trakt.trakt.core.user.usecase.lists.LoadUserWatchlistUseCase

internal class GetWatchlistUseCase(
    private val loadUserWatchlistUseCase: LoadUserWatchlistUseCase,
) {
    suspend fun getLocalWatchlist(limit: Int? = null): ImmutableList<WatchlistItem> {
        return loadUserWatchlistUseCase
            .loadLocalAll()
            .sortedByDescending { it.listedAt }
            .take(limit ?: Int.MAX_VALUE)
            .toImmutableList()
    }

    suspend fun getWatchlist(limit: Int? = null): ImmutableList<WatchlistItem> {
        return loadUserWatchlistUseCase.loadWatchlist()
            .sortedByDescending { it.listedAt }
            .take(limit ?: Int.MAX_VALUE)
            .toImmutableList()
    }
}
