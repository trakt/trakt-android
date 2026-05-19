package tv.trakt.trakt.app.core.home.sections.shows.upnext.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.app.core.home.HomeConfig.HOME_SECTION_LIMIT
import tv.trakt.trakt.app.core.home.sections.shows.upnext.model.ProgressShow
import tv.trakt.trakt.app.core.home.sections.shows.upnext.model.ProgressShow.Progress
import tv.trakt.trakt.app.core.sync.data.remote.shows.ShowsSyncRemoteDataSource
import tv.trakt.trakt.common.core.episodes.data.local.EpisodeLocalDataSource
import tv.trakt.trakt.common.core.shows.data.local.ShowLocalDataSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.toZonedDateTime
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.fromDto

internal class GetShowsUpNextUseCase(
    private val remoteShowsSource: ShowsSyncRemoteDataSource,
    private val localShowSource: ShowLocalDataSource,
    private val localEpisodeSource: EpisodeLocalDataSource,
) {
    suspend fun getShowsUpNext(
        page: Int = 1,
        limit: Int = HOME_SECTION_LIMIT,
    ): ImmutableList<ProgressShow> {
        val remoteItems = remoteShowsSource.getUpNextProgress(
            limit = limit,
            page = page,
            intent = "continue",
            sortHow = null,
            sortBy = null,
        )
        return remoteItems
            .asyncMap { item ->
                val nextEpisode = item.progress.nextEpisode?.let { Episode.fromDto(it) }
                val lastEpisode = item.progress.lastEpisode?.let { Episode.fromDto(it) }
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
                        lastEpisode = lastEpisode,
                        nextEpisode = nextEpisode,
                        // FIXME: progress.last_episode is the user's furthest watched episode, not the
                        //  show's latest aired episode, so we can't compare directly. As a proxy, treat
                        //  the next episode as the latest aired when remaining (aired - completed) is 1
                        //  or less - i.e. no further aired episode exists beyond the displayed one.
                        //  Replace once the API surfaces an absolute "latest aired episode" reference.
                        isLatestAired = (item.progress.aired - item.progress.completed) <= 1,
                    ),
                )
            }
            .toImmutableList()
            .also {
                val shows = it.asyncMap { item -> item.show }
                val episodes = it
                    .asyncMap { item -> item.progress.nextEpisode }
                    .filterNotNull()

                localShowSource.upsertShows(shows)
                localEpisodeSource.upsertEpisodes(episodes)
            }
    }
}
