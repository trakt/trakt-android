package tv.trakt.trakt.app.core.details.show.usecases.collection

import tv.trakt.trakt.app.core.sync.data.local.shows.ShowsSyncLocalDataSource
import tv.trakt.trakt.app.core.sync.data.remote.shows.ShowsSyncRemoteDataSource
import tv.trakt.trakt.app.core.sync.model.WatchedShow
import tv.trakt.trakt.common.helpers.extensions.nowUtc
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.model.DateSelectionResult
import tv.trakt.trakt.common.model.TraktId

internal class ChangeHistoryUseCase(
    private val remoteSource: ShowsSyncRemoteDataSource,
    private val syncLocalSource: ShowsSyncLocalDataSource,
) {
    suspend fun addToHistory(
        showId: TraktId,
        episodesPlays: Int,
        episodesAiredCount: Int,
        customDate: DateSelectionResult? = null,
    ): Int {
        val watchedAt = customDate?.dateString
            ?: nowUtcInstant().toString()

        val response = remoteSource.addToHistory(
            showId = showId,
            watchedAt = watchedAt,
        )

        with(syncLocalSource) {
            val timestamp = nowUtc()
            saveWatched(
                shows = listOf(
                    WatchedShow(
                        showId = showId,
                        episodesPlays = episodesPlays + response.added.episodes,
                        episodesAired = episodesAiredCount,
                        lastWatchedAt = timestamp,
                    ),
                ),
                timestamp = timestamp,
            )
            removeWatchlist(setOf(showId), timestamp)
        }

        return episodesPlays + response.added.episodes
    }

    suspend fun removeFromHistory(showId: TraktId) {
        remoteSource.removeFromHistory(showId = showId)
        syncLocalSource.removeWatched(setOf(showId))
    }
}
