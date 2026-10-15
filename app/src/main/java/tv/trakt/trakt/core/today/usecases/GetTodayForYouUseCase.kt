package tv.trakt.trakt.core.today.usecases

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import tv.trakt.trakt.common.core.user.data.remote.watchlist.UserWatchlistRemoteDataSource
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.model.sorting.SortOrder
import tv.trakt.trakt.common.model.sorting.SortType
import tv.trakt.trakt.common.model.sorting.Sorting
import tv.trakt.trakt.core.sync.data.remote.shows.ShowsSyncRemoteDataSource
import tv.trakt.trakt.core.today.model.TodayForYouItem
import tv.trakt.trakt.core.today.model.TodayMedia
import java.time.Instant
import kotlin.time.Duration.Companion.hours
import kotlin.time.toJavaDuration

private const val RELEASES_LIMIT = 25
private const val WATCHLIST_EXTENDED = "full,cloud9,colors"

private val TodayWindow = 24.hours

internal class GetTodayForYouUseCase(
    private val showsSyncSource: ShowsSyncRemoteDataSource,
    private val watchlistSource: UserWatchlistRemoteDataSource,
) {
    suspend fun getForYou(): List<TodayForYouItem> {
        val now = nowUtcInstant()
        val window = now.minus(TodayWindow.toJavaDuration())..now

        return coroutineScope {
            val upNext = async { getUpNext(window) }
            val watchlist = async { getWatchlist(window) }

            val results = upNext.await() + watchlist.await()
            results
        }
    }

    private suspend fun getUpNext(window: ClosedRange<Instant>): List<TodayForYouItem> {
        return showsSyncSource.getUpNext(
            limit = RELEASES_LIMIT,
            page = 1,
            intent = "continue",
            sortHow = null,
            sortBy = null,
            filters = null,
        ).mapNotNull { item ->
            val episode = item.progress.nextEpisode ?: return@mapNotNull null
            TodayForYouItem.upNext(
                show = Show.fromDto(item.show),
                episode = Episode.fromDto(episode),
                window = window,
            )
        }
    }

    private suspend fun getWatchlist(window: ClosedRange<Instant>): List<TodayForYouItem> {
        return watchlistSource.getWatchlist(
            page = 1,
            limit = RELEASES_LIMIT,
            extended = WATCHLIST_EXTENDED,
            sorting = releasedSorting,
            hide = "unreleased",
            filters = null,
        ).mapNotNull { item ->
            val media = item.movie?.let { TodayMedia.MovieMedia(Movie.fromDto(it)) }
                ?: item.show?.let { TodayMedia.ShowMedia(Show.fromDto(it)) }
                ?: return@mapNotNull null

            TodayForYouItem.startWatching(
                media = media,
                window = window,
            )
        }
    }

    private val releasedSorting = Sorting(
        type = SortType.Released,
        order = SortOrder.Desc,
    )
}
