package tv.trakt.trakt.core.user

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentMapOf
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableSet
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.MediaType.MOVIE
import tv.trakt.trakt.common.model.MediaType.SHOW
import tv.trakt.trakt.common.model.TraktId

@Immutable
internal data class UserCollectionState(
    private val watchedShowsPlays: ImmutableMap<TraktId, Int> = persistentMapOf(),
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
        airedEpisodes: Int?,
    ): Boolean {
        return when (type) {
            SHOW -> {
                check(airedEpisodes != null) { "Aired episodes count must be provided for shows" }
                if (airedEpisodes == 0) return false
                watchedShowsPlays.getOrDefault(traktId, 0) >= airedEpisodes
            }
            MOVIE -> {
                watchedMovies.contains(traktId)
            }
            else -> {
                false
            }
        }
    }
}
