package tv.trakt.trakt.core.home.sections.upnext.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.core.episodes.data.local.EpisodeLocalDataSource
import tv.trakt.trakt.common.core.home.model.Progress
import tv.trakt.trakt.common.core.home.model.UpNextItem
import tv.trakt.trakt.common.core.home.model.UpNextMovie
import tv.trakt.trakt.common.core.home.model.UpNextShow
import tv.trakt.trakt.common.core.movies.data.local.MovieLocalDataSource
import tv.trakt.trakt.common.core.shows.data.local.ShowLocalDataSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.toInstant
import tv.trakt.trakt.common.helpers.extensions.toZonedDateTime
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.core.home.sections.upnext.data.local.HomeUpNextLocalDataSource
import tv.trakt.trakt.core.sync.data.remote.movies.MoviesSyncRemoteDataSource
import tv.trakt.trakt.core.sync.data.remote.shows.ShowsSyncRemoteDataSource

internal class GetUpNextUseCase(
    private val remoteShowsSyncSource: ShowsSyncRemoteDataSource,
    private val remoteMoviesSyncSource: MoviesSyncRemoteDataSource,
    private val localDataSource: HomeUpNextLocalDataSource,
    private val localShowSource: ShowLocalDataSource,
    private val localMovieSource: MovieLocalDataSource,
    private val localEpisodeSource: EpisodeLocalDataSource,
) {
    suspend fun getLocalUpNext(limit: Int): ImmutableList<UpNextItem> {
        return localDataSource.getItems()
            .sortedByDescending { it.sortKey }
            .take(limit)
            .toImmutableList()
    }

    suspend fun getUpNext(
        page: Int,
        limit: Int,
        filters: GlobalFilter?,
        skipLocal: Boolean = false,
    ): ImmutableList<UpNextItem> {
        val shows = getRemoteShows(page, limit, filters)
        val movies = getRemoteMovies(page, limit, filters)

        return (shows + movies)
            .distinctBy { it.key }
            .sortedByDescending { it.sortKey }
            .toImmutableList()
            .also {
                if (!skipLocal) {
                    when (page) {
                        1 -> localDataSource.setItems(items = it)
                        else -> localDataSource.addItems(items = it)
                    }
                }

                val shows = it.filterIsInstance<UpNextShow>().asyncMap { item -> item.show }
                val movies = it.filterIsInstance<UpNextMovie>().asyncMap { item -> item.movie }
                val episodes = it.filterIsInstance<UpNextShow>().asyncMap { item -> item.progress.nextEpisode }

                localShowSource.upsertShows(shows)
                localMovieSource.upsertMovies(movies)
                localEpisodeSource.upsertEpisodes(episodes.filterNotNull())
            }
    }

    private suspend fun getRemoteShows(
        page: Int,
        limit: Int,
        filters: GlobalFilter?,
    ): List<UpNextShow> {
        val remoteItems = remoteShowsSyncSource.getUpNext(
            limit = limit,
            page = page,
            intent = "continue",
            sortHow = null,
            sortBy = null,
            filters = filters,
        )

        return remoteItems
            .mapNotNull { item ->
                val nextEpisode = item.progress.nextEpisode?.let {
                    Episode.fromDto(it)
                } ?: return@mapNotNull null

                UpNextShow(
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
                        lastEpisode = item.progress.lastEpisode?.let { Episode.fromDto(it) },
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
    }

    private suspend fun getRemoteMovies(
        page: Int,
        limit: Int,
        filters: GlobalFilter?,
    ): List<UpNextMovie> {
        val remoteItems = remoteMoviesSyncSource.getPlaybackProgress(
            limit = limit,
            page = page,
            filters = filters,
        )

        return remoteItems
            .asyncMap { item ->
                UpNextMovie(
                    movie = Movie.fromDto(item.movie),
                    progress = UpNextMovie.Progress(
                        id = item.id,
                        progress = item.progress,
                        pausedAt = item.pausedAt.toInstant(),
                    ),
                )
            }
    }
}
