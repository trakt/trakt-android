package tv.trakt.trakt.core.sync.usecases

import org.openapitools.client.models.PostSyncHistoryAdd200Response
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.model.DateSelectionResult
import tv.trakt.trakt.common.model.SeasonEpisode
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.sync.data.remote.episodes.EpisodesSyncRemoteDataSource
import tv.trakt.trakt.widgets.data.WidgetsUpdater

internal class UpdateEpisodeHistoryUseCase(
    private val remoteSource: EpisodesSyncRemoteDataSource,
    private val widgetsUpdater: WidgetsUpdater,
) {
    suspend fun addToHistory(
        episodeId: TraktId,
        customDate: DateSelectionResult? = null,
    ): PostSyncHistoryAdd200Response {
        val watchedAt = customDate?.dateString
            ?: nowUtcInstant().toString()

        return remoteSource.addToHistory(
            episodeId = episodeId,
            watchedAt = watchedAt,
        ).also {
            widgetsUpdater.refreshInBackground()
        }
    }

    suspend fun addToHistory(
        showId: TraktId,
        seasonEpisode: SeasonEpisode,
        customDate: DateSelectionResult? = null,
    ): PostSyncHistoryAdd200Response {
        val watchedAt = customDate?.dateString
            ?: nowUtcInstant().toString()

        return remoteSource.addToHistory(
            showId = showId,
            season = seasonEpisode.season,
            episode = seasonEpisode.episode,
            watchedAt = watchedAt,
        ).also {
            widgetsUpdater.refreshInBackground()
        }
    }

    suspend fun addToHistory(
        episodeIds: List<TraktId>,
        customDate: DateSelectionResult? = null,
    ) {
        val watchedAt = customDate?.dateString
            ?: nowUtcInstant().toString()

        remoteSource.addToHistory(
            episodeIds = episodeIds,
            watchedAt = watchedAt,
        )
        widgetsUpdater.refreshInBackground()
    }

    suspend fun addToHistory(episodes: List<Pair<TraktId, String>>) {
        remoteSource.addToHistory(
            episodes = episodes,
        )
        widgetsUpdater.refreshInBackground()
    }

    suspend fun removeEpisodeFromHistory(episodeId: Int) {
        remoteSource.removeEpisodeFromHistory(
            episodeId = episodeId,
        )
        widgetsUpdater.refreshInBackground()
    }

    suspend fun removeSeasonFromHistory(seasonId: Int) {
        remoteSource.removeSeasonFromHistory(
            seasonId = seasonId,
        )
        widgetsUpdater.refreshInBackground()
    }

    suspend fun removePlayFromHistory(playId: Long) {
        remoteSource.removePlayFromHistory(
            playId = playId,
        )
        widgetsUpdater.refreshInBackground()
    }
}
