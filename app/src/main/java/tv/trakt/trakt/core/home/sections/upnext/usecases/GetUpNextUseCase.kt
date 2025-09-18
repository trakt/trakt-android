package tv.trakt.trakt.core.home.sections.upnext.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.toZonedDateTime
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.core.episodes.model.Episode
import tv.trakt.trakt.core.episodes.model.fromDto
import tv.trakt.trakt.core.home.HomeConfig.HOME_SECTION_LIMIT
import tv.trakt.trakt.core.home.sections.upnext.data.local.HomeUpNextLocalDataSource
import tv.trakt.trakt.core.home.sections.upnext.model.Progress
import tv.trakt.trakt.core.home.sections.upnext.model.ProgressShow
import tv.trakt.trakt.core.sync.data.remote.shows.ShowsSyncRemoteDataSource

internal class GetUpNextUseCase(
    private val remoteSyncSource: ShowsSyncRemoteDataSource,
    private val localDataSource: HomeUpNextLocalDataSource,
) {
    suspend fun getLocalUpNext(): ImmutableList<ProgressShow> {
        return localDataSource.getItems()
            .toImmutableList()
    }

    suspend fun getUpNext(
        page: Int = 1,
        limit: Int = HOME_SECTION_LIMIT,
        notify: Boolean = true,
    ): ImmutableList<ProgressShow> {
        val remoteItems = remoteSyncSource.getUpNext(
            limit = limit,
            page = page,
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
                        nextEpisode = Episode.fromDto(
                            item.progress.nextEpisode,
                        ),
                    ),
                )
            }
            .toImmutableList()
            .also {
                localDataSource.addItems(
                    items = it,
                    notify = notify,
                )
            }
    }
}
