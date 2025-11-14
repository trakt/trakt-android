package tv.trakt.trakt.core.user

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableSet
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableSet
import tv.trakt.trakt.common.model.TraktId

@Immutable
internal data class UserCollectionState(
    private val watchedShows: ImmutableSet<TraktId> = EmptyImmutableSet,
    private val watchedMovies: ImmutableSet<TraktId> = EmptyImmutableSet,
    private val watchlistShows: ImmutableSet<TraktId> = EmptyImmutableSet,
    private val watchlistMovies: ImmutableSet<TraktId> = EmptyImmutableSet,
) {
    companion object Companion {
        val Default = UserCollectionState()
    }

    fun isWatchlist(traktId: TraktId): Boolean {
        return watchlistShows.contains(traktId) || watchlistMovies.contains(traktId)
    }

    fun isWatched(traktId: TraktId): Boolean {
        return watchedShows.contains(traktId) || watchedMovies.contains(traktId)
    }
}
