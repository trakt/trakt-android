package tv.trakt.trakt.core.sync.usecases

import tv.trakt.trakt.common.core.user.usecases.progress.LoadUserProgressUseCase
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.model.DateSelectionResult
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.core.episodes.data.remote.EpisodesRemoteDataSource
import tv.trakt.trakt.core.shows.data.remote.ShowsRemoteDataSource
import tv.trakt.trakt.core.sync.data.remote.episodes.EpisodesSyncRemoteDataSource
import tv.trakt.trakt.core.sync.data.remote.shows.ShowsSyncRemoteDataSource
import tv.trakt.trakt.widgets.data.WidgetsUpdater

internal class UpdateShowHistoryUseCase(
    private val remoteSource: ShowsSyncRemoteDataSource,
    private val remoteEpisodesSyncSource: EpisodesSyncRemoteDataSource,
    private val remoteShowsSource: ShowsRemoteDataSource,
    private val remoteEpisodesSource: EpisodesRemoteDataSource,
    private val loadProgressUseCase: LoadUserProgressUseCase,
    private val widgetsUpdater: WidgetsUpdater,
) {
    suspend fun addToWatched(
        showId: TraktId,
        customDate: DateSelectionResult? = null,
        watchAgain: Boolean = false,
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
    }

    suspend fun removeAllFromHistory(showId: TraktId) {
        remoteSource.removeAllFromHistory(
            showId = showId,
        )
        widgetsUpdater.refreshInBackground()
    }

    suspend fun dropShow(showId: TraktId) {
        remoteSource.dropShow(showId)
        widgetsUpdater.refreshInBackground()
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
