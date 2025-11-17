package tv.trakt.trakt.core.sync.usecases

import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.sync.data.remote.episodes.EpisodesSyncRemoteDataSource
import java.time.Instant

internal class UpdateEpisodeHistoryUseCase(
    private val remoteSource: EpisodesSyncRemoteDataSource,
) {
    suspend fun addToHistory(
        episodeId: TraktId,
        customDate: Instant? = null,
    ) {
        remoteSource.addToHistory(
            episodeId = episodeId,
            watchedAt = customDate ?: nowUtcInstant(),
        )
    }

    suspend fun addToHistory(episodeIds: List<TraktId>) {
        remoteSource.addToHistory(
            episodeIds = episodeIds,
            watchedAt = nowUtcInstant(),
        )
    }

    suspend fun removeEpisodeFromHistory(episodeId: Int) {
        remoteSource.removeEpisodeFromHistory(
            episodeId = episodeId,
        )
    }

    suspend fun removeSeasonFromHistory(seasonId: Int) {
        remoteSource.removeSeasonFromHistory(
            seasonId = seasonId,
        )
    }

    suspend fun removePlayFromHistory(playId: Long) {
        remoteSource.removePlayFromHistory(
            playId = playId,
        )
    }
}
