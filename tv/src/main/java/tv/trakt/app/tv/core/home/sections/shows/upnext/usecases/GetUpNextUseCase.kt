package tv.trakt.app.tv.core.home.sections.shows.upnext.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.app.tv.core.episodes.data.local.EpisodeLocalDataSource
import tv.trakt.app.tv.core.episodes.model.Episode
import tv.trakt.app.tv.core.episodes.model.fromDto
import tv.trakt.app.tv.core.home.sections.shows.upnext.model.Progress
import tv.trakt.app.tv.core.home.sections.shows.upnext.model.ProgressShow
import tv.trakt.app.tv.core.shows.data.local.ShowLocalDataSource
import tv.trakt.app.tv.core.shows.model.Show
import tv.trakt.app.tv.core.shows.model.fromDto
import tv.trakt.app.tv.core.sync.data.remote.shows.ShowsSyncRemoteDataSource
import tv.trakt.app.tv.helpers.extensions.asyncMap
import tv.trakt.app.tv.helpers.extensions.toZonedDateTime

internal class GetUpNextUseCase(
    private val remoteShowsSource: ShowsSyncRemoteDataSource,
    private val localShowSource: ShowLocalDataSource,
    private val localEpisodeSource: EpisodeLocalDataSource,
) {
    suspend fun getUpNext(): ImmutableList<ProgressShow> {
        val remoteItems = remoteShowsSource.getUpNextProgress(limit = 30)
        return remoteItems
            .asyncMap { item ->
                ProgressShow(
                    show = Show.fromDto(item.show),
                    progress = Progress(
                        lastWatchedAt = item.progress.lastWatchedAt?.toZonedDateTime(),
                        aired = item.progress.aired,
                        completed = item.progress.completed,
                        stats = item.progress.stats?.let {
                            Progress.Stats(
                                playCount = it.playCount,
                                minutesWatched = it.minutesWatched,
                                minutesLeft = it.minutesLeft,
                            )
                        },
                        lastEpisode = item.progress.lastEpisode?.let {
                            Episode.fromDto(it)
                        },
                        nextEpisode = Episode.fromDto(item.progress.nextEpisode),
                    ),
                )
            }
            .toImmutableList()
            .also {
                val shows = it.asyncMap { item -> item.show }
                val episodes = it.asyncMap { item -> item.progress.nextEpisode }

                localShowSource.upsertShows(shows)
                localEpisodeSource.upsertEpisodes(episodes)
            }
    }
}
