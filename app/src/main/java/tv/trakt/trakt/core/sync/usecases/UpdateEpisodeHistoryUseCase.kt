package tv.trakt.trakt.core.sync.usecases

import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.sync.data.remote.episodes.EpisodesSyncRemoteDataSource

internal class UpdateEpisodeHistoryUseCase(
    private val remoteSource: EpisodesSyncRemoteDataSource,
) {
    suspend fun addToHistory(episodeId: TraktId) {
        remoteSource.addToHistory(
            episodeId = episodeId,
            watchedAt = nowUtcInstant(),
        )
    }

    suspend fun removeEpisodeFromHistory(episodeId: Int) {
        remoteSource.removeEpisodeFromHistory(
            episodeId = episodeId,
        )
    }

    suspend fun removePlayFromHistory(playId: Long) {
        remoteSource.removePlayFromHistory(
            playId = playId,
        )
    }
}
