package tv.trakt.trakt.app.core.details.show.usecases.collection

import tv.trakt.trakt.app.core.sync.data.local.shows.ShowsSyncLocalDataSource
import tv.trakt.trakt.app.core.sync.model.WatchedShow
import tv.trakt.trakt.common.core.sync.data.remote.shows.ShowsSyncRemoteSource
import tv.trakt.trakt.common.helpers.extensions.nowUtc
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.model.DateSelectionResult
import tv.trakt.trakt.common.model.TraktId

internal class ChangeHistoryUseCase(
    private val remoteSource: ShowsSyncRemoteSource,
    private val syncLocalSource: ShowsSyncLocalDataSource,
) {
    suspend fun addToHistory(
        showId: TraktId,
        episodesPlays: Int,
        episodesPlaysWithoutSpecials: Int,
        episodesAiredCount: Int,
        customDate: DateSelectionResult? = null,
    ): WatchedShow {
        val watchedAt = customDate?.dateString
            ?: nowUtcInstant().toString()

        val response = remoteSource.addToWatched(
            showId = showId,
            watchedAt = watchedAt,
        )

        val timestamp = nowUtc()
        val watched = WatchedShow(
            showId = showId,
            episodesPlays = episodesPlays + response.added.episodes,
            episodesPlaysWithoutSpecials = episodesPlaysWithoutSpecials + response.added.episodes,
            episodesAired = episodesAiredCount,
            lastWatchedAt = timestamp,
        )

        with(syncLocalSource) {
            saveWatched(
                shows = listOf(watched),
                timestamp = timestamp,
            )
            removeWatchlist(setOf(showId), timestamp)
        }

        return watched
    }

    suspend fun removeFromHistory(showId: TraktId) {
        remoteSource.removeAllFromHistory(showId = showId)
        syncLocalSource.removeWatched(setOf(showId))
    }
}
