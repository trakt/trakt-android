package tv.trakt.trakt.tv.core.details.episode.usecases.collection

import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.tv.core.sync.data.remote.episodes.EpisodesSyncRemoteDataSource
import tv.trakt.trakt.tv.helpers.extensions.nowUtc

internal class ChangeHistoryUseCase(
    private val remoteSource: EpisodesSyncRemoteDataSource,
) {
    suspend fun addToHistory(episodeId: TraktId) {
        val watchedAt = nowUtc()

        remoteSource.addToHistory(
            episodeId = episodeId,
            watchedAt = watchedAt,
        )
    }

    suspend fun removeFromHistory(episodePlayId: Long) {
        remoteSource.removeFromHistory(episodePlayId = episodePlayId)
    }
}
