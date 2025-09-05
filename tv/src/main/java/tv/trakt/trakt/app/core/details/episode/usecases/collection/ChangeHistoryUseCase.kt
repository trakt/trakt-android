package tv.trakt.trakt.app.core.details.episode.usecases.collection

import tv.trakt.trakt.app.core.sync.data.remote.episodes.EpisodesSyncRemoteDataSource
import tv.trakt.trakt.common.helpers.extensions.nowUtc
import tv.trakt.trakt.common.model.TraktId

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
