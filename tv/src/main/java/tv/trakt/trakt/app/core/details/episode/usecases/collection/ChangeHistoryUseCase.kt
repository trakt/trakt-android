package tv.trakt.trakt.app.core.details.episode.usecases.collection

import tv.trakt.trakt.app.core.sync.data.remote.episodes.EpisodesSyncRemoteDataSource
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.model.DateSelectionResult
import tv.trakt.trakt.common.model.TraktId

internal class ChangeHistoryUseCase(
    private val remoteSource: EpisodesSyncRemoteDataSource,
) {
    suspend fun addToHistory(
        episodeId: TraktId,
        customDate: DateSelectionResult? = null,
    ) {
        val watchedAt = customDate?.dateString
            ?: nowUtcInstant().toString()

        remoteSource.addToHistory(
            episodeId = episodeId,
            watchedAt = watchedAt,
        )
    }

    suspend fun removeFromHistory(episodePlayId: Long) {
        remoteSource.removeFromHistory(episodePlayId = episodePlayId)
    }

    suspend fun removeEpisodeFromHistory(episodeId: TraktId) {
        remoteSource.removeEpisodeFromHistory(episodeId = episodeId)
    }
}
