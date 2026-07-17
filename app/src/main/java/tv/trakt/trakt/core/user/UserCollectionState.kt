package tv.trakt.trakt.core.user

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentMapOf
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableSet
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.MediaType.Movie
import tv.trakt.trakt.common.model.MediaType.Show
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
            Show -> watchlistShows.contains(traktId)
            Movie -> watchlistMovies.contains(traktId)
            else -> false
        }
    }

    fun isWatched(
        traktId: TraktId,
        type: MediaType?,
        airedEpisodes: Int?,
    ): Boolean {
        return when (type) {
            Show -> {
                check(airedEpisodes != null) { "Aired episodes count must be provided for shows" }
                if (airedEpisodes == 0) return false
                watchedShowsPlays.getOrDefault(traktId, 0) >= airedEpisodes
            }
            Movie -> {
                watchedMovies.contains(traktId)
            }
            else -> {
                false
            }
        }
    }

    fun isWatching(
        traktId: TraktId,
        type: MediaType?,
        airedEpisodes: Int?,
    ): Boolean {
        return when (type) {
            Show -> {
                check(airedEpisodes != null) { "Aired episodes count must be provided for shows" }
                if (airedEpisodes == 0) return false
                val watchedPlays = watchedShowsPlays.getOrDefault(traktId, 0)
                watchedPlays in 1 until airedEpisodes
            }
            else -> {
                false
            }
        }
    }
}
