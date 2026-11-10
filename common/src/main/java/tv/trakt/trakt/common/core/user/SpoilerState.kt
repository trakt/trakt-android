package tv.trakt.trakt.common.core.user

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.TraktId

@Immutable
data class SpoilerState(
    val enabled: Boolean = false,
    private val collection: UserCollectionState = UserCollectionState.Default,
) {
    fun isMovieHidden(movieId: TraktId): Boolean {
        return enabled && !collection.isWatched(movieId, MediaType.Movie, null)
    }

    fun isEpisodeHidden(episodeId: TraktId): Boolean {
        return enabled && !collection.isEpisodeWatched(episodeId)
    }

    fun isShowHidden(
        showId: TraktId,
        airedEpisodes: Int?,
    ): Boolean {
        if (!enabled) return false
        if (airedEpisodes == null || airedEpisodes <= 0) return true

        return !collection.isWatched(showId, MediaType.Show, airedEpisodes)
    }
}
