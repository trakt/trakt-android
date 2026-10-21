package tv.trakt.trakt.core.sync.usecases

import org.openapitools.client.models.PostSyncHistoryAdd200Response
import tv.trakt.trakt.common.data.remote.EpisodesSyncRemoteSource
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.model.DateSelectionResult
import tv.trakt.trakt.common.model.SeasonEpisode
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.toTraktId
import tv.trakt.trakt.core.ratings.HistoryRemoval
import tv.trakt.trakt.core.ratings.RemoveOrphanedRatingUseCase
import tv.trakt.trakt.widgets.data.WidgetsUpdater

internal class UpdateEpisodeHistoryUseCase(
    private val remoteSource: EpisodesSyncRemoteSource,
    private val widgetsUpdater: WidgetsUpdater,
    private val orphanedRatings: RemoveOrphanedRatingUseCase,
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
        val orphanedRating = orphanedRatings.findEpisode(episodeId.toTraktId(), HistoryRemoval.AllPlays)
        remoteSource.removeEpisodeFromHistory(
            episodeId = episodeId,
        )
        orphanedRatings.remove(orphanedRating)
        widgetsUpdater.refreshInBackground()
    }

    suspend fun removeSeasonFromHistory(seasonId: Int) {
        remoteSource.removeSeasonFromHistory(
            seasonId = seasonId,
        )
        widgetsUpdater.refreshInBackground()
    }

    suspend fun removePlayFromHistory(
        playId: Long,
        episodeId: TraktId,
    ) {
        val orphanedRating = orphanedRatings.findEpisode(episodeId, HistoryRemoval.SinglePlay)
        remoteSource.removePlayFromHistory(
            playId = playId,
        )
        orphanedRatings.remove(orphanedRating)
        widgetsUpdater.refreshInBackground()
    }
}
