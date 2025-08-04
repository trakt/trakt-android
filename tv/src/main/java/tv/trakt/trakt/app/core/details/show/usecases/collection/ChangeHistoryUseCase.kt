package tv.trakt.trakt.app.core.details.show.usecases.collection

import tv.trakt.trakt.app.core.sync.data.local.shows.ShowsSyncLocalDataSource
import tv.trakt.trakt.app.core.sync.data.remote.shows.ShowsSyncRemoteDataSource
import tv.trakt.trakt.app.core.sync.model.WatchedShow
import tv.trakt.trakt.app.helpers.extensions.nowUtc
import tv.trakt.trakt.common.model.TraktId

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
