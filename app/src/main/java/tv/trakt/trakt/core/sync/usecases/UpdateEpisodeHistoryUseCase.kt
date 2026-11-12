package tv.trakt.trakt.core.sync.usecases

import org.openapitools.client.models.PostSyncHistoryAdd200Response
import tv.trakt.trakt.common.core.sync.changes.ChangeOrigin
import tv.trakt.trakt.common.core.sync.changes.DataChange.Kind
import tv.trakt.trakt.common.core.sync.changes.DataChanges
import tv.trakt.trakt.common.core.user.usecases.progress.LoadUserProgressUseCase
import tv.trakt.trakt.common.data.local.UserWatchlistLocalSource
import tv.trakt.trakt.common.data.local.UserWatchlistMinimalLocalSource
import tv.trakt.trakt.common.data.remote.EpisodesSyncRemoteSource
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.model.DateSelectionResult
import tv.trakt.trakt.common.model.SeasonEpisode
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.toTraktId
import tv.trakt.trakt.core.ratings.RemoveOrphanedRatingUseCase
import tv.trakt.trakt.widgets.data.WidgetsUpdater

/**
 * Every write reloads the shows progress and then publishes the history change. Callers that
 * refresh the progress on their own, without blocking the user, pass `refreshProgress = false`.
 */
internal class UpdateEpisodeHistoryUseCase(
    private val remoteSource: EpisodesSyncRemoteSource,
    private val widgetsUpdater: WidgetsUpdater,
    private val orphanedRatings: RemoveOrphanedRatingUseCase,
    private val loadProgressUseCase: LoadUserProgressUseCase,
    private val userWatchlistLocalSource: UserWatchlistLocalSource,
    private val userWatchlistMinLocalSource: UserWatchlistMinimalLocalSource,
    private val dataChanges: DataChanges,
) {
    suspend fun addToHistory(
        episodeId: TraktId,
        customDate: DateSelectionResult? = null,
        origin: ChangeOrigin? = null,
        refreshProgress: Boolean = true,
    ): PostSyncHistoryAdd200Response {
        val watchedAt = customDate?.dateString
            ?: nowUtcInstant().toString()

        val response = remoteSource.addToHistory(
            episodeId = episodeId,
            watchedAt = watchedAt,
        )
        widgetsUpdater.refreshInBackground()

        if (refreshProgress) {
            loadProgressUseCase.loadShowsProgress()
        }
        dataChanges.notify(Kind.History, origin = origin)

        return response
    }

    suspend fun addToHistory(
        showId: TraktId,
        seasonEpisode: SeasonEpisode,
        customDate: DateSelectionResult? = null,
        origin: ChangeOrigin? = null,
        refreshProgress: Boolean = true,
    ): PostSyncHistoryAdd200Response {
        val watchedAt = customDate?.dateString
            ?: nowUtcInstant().toString()

        val response = remoteSource.addToHistory(
            showId = showId,
            season = seasonEpisode.season,
            episode = seasonEpisode.episode,
            watchedAt = watchedAt,
        )
        widgetsUpdater.refreshInBackground()

        if (refreshProgress) {
            loadProgressUseCase.loadShowsProgress()
        }
        userWatchlistLocalSource.removeShows(ids = setOf(showId))
        userWatchlistMinLocalSource.removeShows(ids = setOf(showId))
        dataChanges.notify(Kind.History, Kind.Watchlist, origin = origin)

        return response
    }

    suspend fun addToHistory(
        episodeIds: List<TraktId>,
        customDate: DateSelectionResult? = null,
        origin: ChangeOrigin? = null,
        refreshProgress: Boolean = true,
    ) {
        val watchedAt = customDate?.dateString
            ?: nowUtcInstant().toString()

        remoteSource.addToHistory(
            episodeIds = episodeIds,
            watchedAt = watchedAt,
        )
        widgetsUpdater.refreshInBackground()

        if (refreshProgress) {
            loadProgressUseCase.loadShowsProgress()
        }
        dataChanges.notify(Kind.History, origin = origin)
    }

    suspend fun addToHistory(episodes: List<Pair<TraktId, String>>) {
        remoteSource.addToHistory(
            episodes = episodes,
        )
        widgetsUpdater.refreshInBackground()

        loadProgressUseCase.loadShowsProgress()
        dataChanges.notify(Kind.History)
    }

    suspend fun removeEpisodeFromHistory(
        episodeId: Int,
        origin: ChangeOrigin? = null,
        refreshProgress: Boolean = true,
    ) {
        removeEpisode(episodeId)
        widgetsUpdater.refreshInBackground()

        if (refreshProgress) {
            loadProgressUseCase.loadShowsProgress()
        }
        dataChanges.notify(Kind.History, origin = origin)
    }

    suspend fun removeEpisodesFromHistory(episodeIds: List<TraktId>) {
        episodeIds.forEach { removeEpisode(it.value) }
        widgetsUpdater.refreshInBackground()

        loadProgressUseCase.loadShowsProgress()
        dataChanges.notify(Kind.History)
    }

    suspend fun removeSeasonFromHistory(
        seasonId: Int,
        origin: ChangeOrigin? = null,
    ) {
        remoteSource.removeSeasonFromHistory(
            seasonId = seasonId,
        )
        widgetsUpdater.refreshInBackground()

        loadProgressUseCase.loadShowsProgress()
        dataChanges.notify(Kind.History, origin = origin)
    }

    suspend fun removePlayFromHistory(
        playId: Long,
        episodeId: TraktId,
        origin: ChangeOrigin? = null,
        refreshProgress: Boolean = true,
    ) {
        val orphanedRating = orphanedRatings.findEpisode(episodeId)
        remoteSource.removePlayFromHistory(
            playId = playId,
        )
        orphanedRatings.remove(orphanedRating)
        widgetsUpdater.refreshInBackground()

        if (refreshProgress) {
            loadProgressUseCase.loadShowsProgress()
        }
        dataChanges.notify(Kind.History, origin = origin)
    }

    private suspend fun removeEpisode(episodeId: Int) {
        val orphanedRating = orphanedRatings.findEpisode(episodeId.toTraktId())
        remoteSource.removeEpisodeFromHistory(
            episodeId = episodeId,
        )
        orphanedRatings.remove(orphanedRating)
    }
}
