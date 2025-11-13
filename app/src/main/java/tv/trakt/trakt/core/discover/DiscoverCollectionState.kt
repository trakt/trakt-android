package tv.trakt.trakt.core.discover

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableSet
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableSet
import tv.trakt.trakt.common.model.TraktId

@Immutable
internal data class DiscoverCollectionState(
    val watchedShows: ImmutableSet<TraktId> = EmptyImmutableSet,
    val watchedMovies: ImmutableSet<TraktId> = EmptyImmutableSet,
    val watchlistShows: ImmutableSet<TraktId> = EmptyImmutableSet,
    val watchlistMovies: ImmutableSet<TraktId> = EmptyImmutableSet,
) {
    companion object {
        val Default = DiscoverCollectionState()
    }

    fun isWatchlist(traktId: TraktId): Boolean {
        return watchlistShows.contains(traktId) || watchlistMovies.contains(traktId)
    }

    fun isWatched(traktId: TraktId): Boolean {
        return watchedShows.contains(traktId) || watchedMovies.contains(traktId)
    }
}
