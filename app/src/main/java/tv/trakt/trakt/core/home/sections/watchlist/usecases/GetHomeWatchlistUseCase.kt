package tv.trakt.trakt.core.home.sections.watchlist.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.nowLocalDay
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.helpers.extensions.toZonedDateTime
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.core.home.sections.upnext.model.Progress
import tv.trakt.trakt.core.home.sections.watchlist.data.local.HomeWatchlistLocalDataSource
import tv.trakt.trakt.core.lists.sections.watchlist.model.WatchlistItem
import tv.trakt.trakt.core.main.model.MediaMode
import tv.trakt.trakt.core.sync.data.remote.shows.ShowsSyncRemoteDataSource
import tv.trakt.trakt.core.user.usecases.lists.LoadUserWatchlistUseCase

internal class GetHomeWatchlistUseCase(
    private val loadUserWatchlistUseCase: LoadUserWatchlistUseCase,
    private val remoteShowsSyncSource: ShowsSyncRemoteDataSource,
    private val homeWatchlistLocalSource: HomeWatchlistLocalDataSource,
) {
    suspend fun getLocalWatchlist(
        limit: Int? = null,
        filter: MediaMode = MediaMode.MEDIA,
    ): ImmutableList<WatchlistItem> {
        return coroutineScope {
            val nowDay = nowLocalDay()

            val showsAsync = async {
                homeWatchlistLocalSource.getItems()
            }

            val moviesAsync = async {
                loadUserWatchlistUseCase.loadLocalMovies()
                    .filter {
                        it.movie.released != null && it.movie.released!! <= nowDay
                    }
            }

            (showsAsync.await() + moviesAsync.await())
                .filter {
                    when (filter) {
                        MediaMode.MEDIA -> true
                        MediaMode.SHOWS -> it is WatchlistItem.ShowItem
                        MediaMode.MOVIES -> it is WatchlistItem.MovieItem
                    }
                }
                .sortedWith(
                    compareByDescending<WatchlistItem> { it.released }
                        .thenBy { it.title },
                )
                .take(limit ?: Int.MAX_VALUE)
                .toImmutableList()
        }
    }

    suspend fun getWatchlist(
        limit: Int? = null,
        filter: MediaMode = MediaMode.MEDIA,
    ): ImmutableList<WatchlistItem> {
        return coroutineScope {
            val nowDay = nowLocalDay()

            val showsAsync = async {
                remoteShowsSyncSource.getUpNext(
                    limit = limit ?: Int.MAX_VALUE,
                    page = 1,
                    intent = "start",
                ).asyncMap { item ->
                    WatchlistItem.ShowItem(
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
                        rank = 0,
                        listedAt = nowUtcInstant(),
                    )
                }.also {
                    homeWatchlistLocalSource.setItems(
                        items = it,
                        notify = false,
                    )
                }
            }

            val moviesAsync = async {
                loadUserWatchlistUseCase.loadWatchlist()
                    .filterIsInstance<WatchlistItem.MovieItem>()
                    .filter {
                        it.movie.released != null &&
                            it.movie.released!! <= nowDay
                    }
            }

            (showsAsync.await() + moviesAsync.await())
                .filter {
                    when (filter) {
                        MediaMode.MEDIA -> true
                        MediaMode.SHOWS -> it is WatchlistItem.ShowItem
                        MediaMode.MOVIES -> it is WatchlistItem.MovieItem
                    }
                }
                .sortedWith(
                    compareByDescending<WatchlistItem> { it.released }
                        .thenBy { it.title },
                )
                .take(limit ?: Int.MAX_VALUE)
                .toImmutableList()
        }
    }
}
