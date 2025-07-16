package tv.trakt.app.tv.core.details.episode.usecases.collection

import tv.trakt.app.tv.common.model.TraktId
import tv.trakt.app.tv.core.sync.data.remote.episodes.EpisodesSyncRemoteDataSource
import tv.trakt.app.tv.helpers.extensions.nowUtc

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
