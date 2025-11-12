package tv.trakt.trakt.core.home.sections.upnext.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.core.episodes.data.local.EpisodeLocalDataSource
import tv.trakt.trakt.common.core.shows.data.local.ShowLocalDataSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.toZonedDateTime
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.core.home.sections.upnext.data.local.HomeUpNextLocalDataSource
import tv.trakt.trakt.core.home.sections.upnext.model.Progress
import tv.trakt.trakt.core.home.sections.upnext.model.ProgressShow
import tv.trakt.trakt.core.sync.data.remote.shows.ShowsSyncRemoteDataSource

internal class GetUpNextUseCase(
    private val remoteSyncSource: ShowsSyncRemoteDataSource,
    private val localDataSource: HomeUpNextLocalDataSource,
    private val localShowSource: ShowLocalDataSource,
    private val localEpisodeSource: EpisodeLocalDataSource,
) {
    suspend fun getLocalUpNext(limit: Int): ImmutableList<ProgressShow> {
        return localDataSource.getItems()
            .take(limit)
            .toImmutableList()
    }

    suspend fun getUpNext(
        page: Int,
        limit: Int,
        notify: Boolean,
    ): ImmutableList<ProgressShow> {
        val remoteItems = remoteSyncSource.getUpNext(
            limit = limit,
            page = page,
            intent = "continue",
        )
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
                when (page) {
                    1 -> localDataSource.setItems(
                        items = it,
                        notify = notify,
                    )
                    else -> localDataSource.addItems(
                        items = it,
                        notify = notify,
                    )
                }

                val shows = it.asyncMap { item -> item.show }
                val episodes = it.asyncMap { item -> item.progress.nextEpisode }

                localShowSource.upsertShows(shows)
                localEpisodeSource.upsertEpisodes(episodes)
            }
    }
}
