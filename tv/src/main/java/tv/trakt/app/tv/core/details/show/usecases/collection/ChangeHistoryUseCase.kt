package tv.trakt.app.tv.core.details.show.usecases.collection

import tv.trakt.app.tv.common.model.TraktId
import tv.trakt.app.tv.core.sync.data.local.shows.ShowsSyncLocalDataSource
import tv.trakt.app.tv.core.sync.data.remote.shows.ShowsSyncRemoteDataSource
import tv.trakt.app.tv.core.sync.model.WatchedShow
import tv.trakt.app.tv.helpers.extensions.nowUtc

internal class ChangeHistoryUseCase(
    private val remoteSource: ShowsSyncRemoteDataSource,
    private val syncLocalSource: ShowsSyncLocalDataSource,
) {
    suspend fun addToHistory(
        showId: TraktId,
        episodesPlays: Int,
        episodesAiredCount: Int,
    ): Int {
        val watchedAt = nowUtc()

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
                        episodesAiredCount = episodesAiredCount,
                        lastWatchedAt = watchedAt,
                    ),
                ),
                timestamp = timestamp,
            )
            removeWatchlist(setOf(showId), timestamp)
        }

        return episodesPlays + response.added.episodes
    }
}
