package tv.trakt.trakt.app.core.details.show.usecases.collection

import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.app.core.sync.data.local.shows.ShowsSyncLocalDataSource
import tv.trakt.trakt.app.core.sync.data.remote.shows.ShowsSyncRemoteDataSource
import tv.trakt.trakt.app.core.sync.model.WatchedShow
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.toZonedDateTime
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.toTraktId

internal class GetCollectionUseCase(
    private val remoteSource: ShowsSyncRemoteDataSource,
    private val syncLocalSource: ShowsSyncLocalDataSource,
) {
    suspend fun getWatchedShow(show: Show): WatchedShow? {
        var localWatched = syncLocalSource.getWatched()
        if (localWatched == null) {
            val remoteWatched = remoteSource
                .getWatched()
                .map { entry ->
                    val showId = entry.key.toInt().toTraktId()

                    val seasons = entry.value.map { seasonKey ->
                        val episodes = seasonKey.value.map { episodeKey ->
                            val episodePlays = episodeKey.value
                            WatchedShow.Episode(
                                id = episodeKey.key.toInt().toTraktId(),
                                plays = episodePlays.size,
                                playsDistinct = if (episodePlays.isEmpty()) 0 else 1,
                                lastWatchedAt = episodePlays.maxOf { it.toZonedDateTime() },
                            )
                        }

                        val (seasonId, seasonNumber) = seasonKey.key.split("|")
                        WatchedShow.Season(
                            id = seasonId.toInt().toTraktId(),
                            number = seasonNumber.toInt(),
                            episodes = episodes.toImmutableList(),
                        )
                    }

                    WatchedShow(
                        showId = showId,
                        episodesPlays = seasons
                            .flatMap { it.episodes }
                            .sumOf { it.plays },
                        episodesAired = show.airedEpisodes,
                        lastWatchedAt = seasons
                            .flatMap { it.episodes }
                            .maxOf { it.lastWatchedAt },
                    )
                }

            syncLocalSource.saveWatched(remoteWatched, null)
            localWatched = remoteWatched.associateBy { it.showId }
        }

        return localWatched[show.ids.trakt]
    }

    suspend fun getWatchlistShow(showId: TraktId): TraktId? {
        var localWatchlist = syncLocalSource.getWatchlist()

        if (localWatchlist == null) {
            val remoteWatchlist = remoteSource
                .getWatchlist(sort = "added")
                .asyncMap { it.show.ids.trakt.toTraktId() }
                .toSet()

            syncLocalSource.saveWatchlist(remoteWatchlist, null)
            localWatchlist = remoteWatchlist.toSet()
        }

        return localWatchlist
            .find { it.value == showId.value }
    }
}
