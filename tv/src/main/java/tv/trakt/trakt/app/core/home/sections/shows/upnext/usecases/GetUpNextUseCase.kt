package tv.trakt.trakt.app.core.home.sections.shows.upnext.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.app.core.episodes.data.local.EpisodeLocalDataSource
import tv.trakt.trakt.app.core.episodes.model.Episode
import tv.trakt.trakt.app.core.episodes.model.fromDto
import tv.trakt.trakt.app.core.home.HomeConfig.UP_NEXT_SECTION_LIMIT
import tv.trakt.trakt.app.core.home.sections.shows.upnext.model.Progress
import tv.trakt.trakt.app.core.home.sections.shows.upnext.model.ProgressShow
import tv.trakt.trakt.app.core.shows.data.local.ShowLocalDataSource
import tv.trakt.trakt.app.core.sync.data.remote.shows.ShowsSyncRemoteDataSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.toZonedDateTime
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.fromDto

internal class GetUpNextUseCase(
    private val remoteShowsSource: ShowsSyncRemoteDataSource,
    private val localShowSource: ShowLocalDataSource,
    private val localEpisodeSource: EpisodeLocalDataSource,
) {
    suspend fun getUpNext(
        page: Int = 1,
        limit: Int = UP_NEXT_SECTION_LIMIT,
    ): ImmutableList<ProgressShow> {
        val remoteItems = remoteShowsSource.getUpNextProgress(
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
