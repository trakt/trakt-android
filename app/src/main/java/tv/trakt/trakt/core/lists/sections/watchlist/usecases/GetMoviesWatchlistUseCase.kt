package tv.trakt.trakt.core.lists.sections.watchlist.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.core.lists.sections.watchlist.model.WatchlistItem
import tv.trakt.trakt.core.user.usecases.lists.LoadUserWatchlistUseCase

internal class GetMoviesWatchlistUseCase(
    private val loadUserWatchlistUseCase: LoadUserWatchlistUseCase,
) {
    suspend fun getLocalWatchlist(limit: Int? = null): ImmutableList<WatchlistItem> {
        return loadUserWatchlistUseCase
            .loadLocalMovies()
            .sortedByDescending { it.listedAt }
            .take(limit ?: Int.MAX_VALUE)
            .toImmutableList()
    }

    suspend fun getWatchlist(limit: Int? = null): ImmutableList<WatchlistItem> {
        return loadUserWatchlistUseCase.loadWatchlist()
            .filterIsInstance<WatchlistItem.MovieItem>()
            .sortedByDescending { it.listedAt }
            .take(limit ?: Int.MAX_VALUE)
            .toImmutableList()
    }
}
