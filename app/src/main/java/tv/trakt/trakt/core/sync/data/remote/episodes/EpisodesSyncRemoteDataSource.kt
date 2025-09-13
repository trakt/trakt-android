package tv.trakt.trakt.core.sync.data.remote.episodes

import tv.trakt.trakt.common.model.TraktId
import java.time.Instant

internal interface EpisodesSyncRemoteDataSource {
    suspend fun addToHistory(
        episodeId: TraktId,
        watchedAt: Instant,
    )
}
