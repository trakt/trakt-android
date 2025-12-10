package tv.trakt.trakt.core.user

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableSet
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableSet
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.MediaType.MOVIE
import tv.trakt.trakt.common.model.MediaType.SHOW
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

    fun isWatchlist(
        traktId: TraktId,
        type: MediaType?,
    ): Boolean {
        return when (type) {
            SHOW -> watchlistShows.contains(traktId)
            MOVIE -> watchlistMovies.contains(traktId)
            else -> false
        }
    }

    fun isWatched(
        traktId: TraktId,
        type: MediaType?,
    ): Boolean {
        return when (type) {
            SHOW -> watchedShows.contains(traktId)
            MOVIE -> watchedMovies.contains(traktId)
            else -> false
        }
    }
}
