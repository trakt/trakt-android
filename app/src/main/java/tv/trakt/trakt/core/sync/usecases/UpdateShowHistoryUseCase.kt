package tv.trakt.trakt.core.sync.usecases

import tv.trakt.trakt.common.core.sync.changes.ChangeOrigin
import tv.trakt.trakt.common.core.sync.changes.DataChange.Kind
import tv.trakt.trakt.common.core.sync.changes.DataChanges
import tv.trakt.trakt.common.core.user.usecases.progress.LoadUserProgressUseCase
import tv.trakt.trakt.common.data.local.HomeUpNextLocalSource
import tv.trakt.trakt.common.data.local.HomeUpcomingLocalSource
import tv.trakt.trakt.common.data.local.UserProgressLocalSource
import tv.trakt.trakt.common.data.local.UserWatchlistLocalSource
import tv.trakt.trakt.common.data.local.UserWatchlistMinimalLocalSource
import tv.trakt.trakt.common.data.remote.EpisodesRemoteSource
import tv.trakt.trakt.common.data.remote.EpisodesSyncRemoteSource
import tv.trakt.trakt.common.data.remote.ShowsRemoteSource
import tv.trakt.trakt.common.data.remote.ShowsSyncRemoteSource
import tv.trakt.trakt.common.firebase.analytics.Analytics
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.model.DateSelectionResult
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.core.ratings.RemoveOrphanedRatingUseCase
import tv.trakt.trakt.widgets.data.WidgetsUpdater

internal class UpdateShowHistoryUseCase(
    private val remoteSource: ShowsSyncRemoteSource,
    private val remoteEpisodesSyncSource: EpisodesSyncRemoteSource,
    private val remoteShowsSource: ShowsRemoteSource,
    private val remoteEpisodesSource: EpisodesRemoteSource,
    private val loadProgressUseCase: LoadUserProgressUseCase,
    private val widgetsUpdater: WidgetsUpdater,
    private val analytics: Analytics,
    private val orphanedRatings: RemoveOrphanedRatingUseCase,
    private val userProgressLocalSource: UserProgressLocalSource,
    private val userWatchlistLocalSource: UserWatchlistLocalSource,
    private val userWatchlistMinLocalSource: UserWatchlistMinimalLocalSource,
    private val upNextLocalSource: HomeUpNextLocalSource,
    private val upcomingLocalSource: HomeUpcomingLocalSource,
    private val dataChanges: DataChanges,
) {
    suspend fun addToWatched(
        showId: TraktId,
        customDate: DateSelectionResult? = null,
        watchAgain: Boolean = false,
        origin: ChangeOrigin? = null,
    ) {
        val watchedAt = customDate?.dateString
            ?: nowUtcInstant().toString()

        val watchedIds = when (loadProgressUseCase.isShowsLoaded()) {
            true -> loadProgressUseCase.loadLocalShows()
            false -> loadProgressUseCase.loadShowsProgress()
        }
            .firstOrNull { it.showId == showId }
            ?.seasons
            ?.flatMap { season -> season.episodes.map { it.id } }
            ?.toSet()
            .orEmpty()

        if (watchedIds.isEmpty() || watchAgain) {
            remoteSource.addToWatched(
                showId = showId,
                watchedAt = watchedAt,
            )
        } else {
            val unwatchedIds = getUnwatchedEpisodeIds(showId, watchedIds)
            if (unwatchedIds.isNotEmpty()) {
                remoteEpisodesSyncSource.addToHistory(
                    episodeIds = unwatchedIds,
                    watchedAt = watchedAt,
                )
            }
        }

        widgetsUpdater.refreshInBackground()

        loadProgressUseCase.loadShowsProgress()
        userWatchlistLocalSource.removeShows(ids = setOf(showId))
        userWatchlistMinLocalSource.removeShows(ids = setOf(showId))
        dataChanges.notify(Kind.History, Kind.Watchlist, origin = origin)
    }

    suspend fun removeAllFromHistory(
        showId: TraktId,
        origin: ChangeOrigin? = null,
    ) {
        val orphanedRating = orphanedRatings.findShow(showId)
        remoteSource.removeAllFromHistory(
            showId = showId,
        )
        orphanedRatings.remove(orphanedRating)
        widgetsUpdater.refreshInBackground()

        userProgressLocalSource.removeShows(ids = setOf(showId))
        dataChanges.notify(Kind.History, Kind.Progress, origin = origin)
    }

    suspend fun dropShow(
        showId: TraktId,
        refreshProgress: Boolean = true,
    ) {
        remoteSource.dropShow(showId)
        analytics.progress.logDrop(MediaType.Show.value)
        widgetsUpdater.refreshInBackground()

        upNextLocalSource.removeShowItems(ids = listOf(showId))
        upcomingLocalSource.removeShowItems(
            showIds = listOf(showId),
            notify = true,
        )
        dataChanges.notify(Kind.History)
        if (refreshProgress) {
            loadProgressUseCase.loadShowsProgress()
        }
    }

    private suspend fun getUnwatchedEpisodeIds(
        showId: TraktId,
        watchedIds: Set<TraktId>,
    ): List<TraktId> {
        return remoteShowsSource.getSeasons(showId)
            .filter { it.number > 0 }
            .asyncMap { season ->
                remoteEpisodesSource.getSeason(
                    showId = showId,
                    season = season.number,
                ).asyncMap { Episode.fromDto(it) }
            }
            .flatten()
            .filter { it.isReleased && it.ids.trakt !in watchedIds }
            .map { it.ids.trakt }
    }
}
