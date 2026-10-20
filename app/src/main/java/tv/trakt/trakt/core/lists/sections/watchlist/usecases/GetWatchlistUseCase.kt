package tv.trakt.trakt.core.lists.sections.watchlist.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.core.lists.model.WatchlistItem
import tv.trakt.trakt.common.data.local.UserWatchlistLocalSource
import tv.trakt.trakt.common.data.remote.UserWatchlistRemoteSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.toInstant
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.common.model.ratings.UserRating
import tv.trakt.trakt.common.model.sorting.Sorting
import tv.trakt.trakt.core.lists.sections.watchlist.model.getWatchlistSorting

internal class GetWatchlistUseCase(
    private val remoteSource: UserWatchlistRemoteSource,
    private val userWatchlistLocalSource: UserWatchlistLocalSource,
) {
    suspend fun getLocalWatchlist(
        limit: Int,
        sorting: Sorting,
    ): ImmutableList<WatchlistItem> {
        return userWatchlistLocalSource.getAll()
            .sortedWith(getWatchlistSorting(sorting))
            .take(limit)
            .toImmutableList()
    }

    suspend fun getRemoteWatchlist(
        page: Int,
        limit: Int,
        sorting: Sorting,
        filters: GlobalFilter,
        showsRatings: Map<TraktId, UserRating>? = null,
        moviesRatings: Map<TraktId, UserRating>? = null,
        skipLocal: Boolean = false,
    ): ImmutableList<WatchlistItem> {
        val response = remoteSource.getWatchlist(
            page = page,
            limit = limit,
            sorting = sorting,
            extended = "full,images,colors",
            filters = filters,
        ).asyncMap {
            val listedAt = it.listedAt.toInstant()

            when {
                it.movie != null -> {
                    val movie = Movie.fromDto(it.movie!!)
                    WatchlistItem.MovieItem(
                        movie = movie,
                        rank = it.rank,
                        listedAt = listedAt,
                        userRating = moviesRatings?.get(movie.ids.trakt),
                    )
                }

                it.show != null -> {
                    val show = Show.fromDto(it.show!!)
                    WatchlistItem.ShowItem(
                        show = show,
                        rank = it.rank,
                        listedAt = listedAt,
                        userRating = showsRatings?.get(show.ids.trakt),
                    )
                }

                else -> {
                    throw IllegalStateException("Watchlist item unknown type!")
                }
            }
        }

        return response
            .sortedWith(getWatchlistSorting(sorting))
            .toImmutableList()
            .also {
                if (skipLocal) {
                    return@also
                }

                val shows = response.filterIsInstance<WatchlistItem.ShowItem>()
                val movies = response.filterIsInstance<WatchlistItem.MovieItem>()

                with(userWatchlistLocalSource) {
                    if (page == 1) {
                        setShows(shows)
                        setMovies(movies)
                    } else {
                        addShows(shows)
                        addMovies(movies)
                    }
                }
            }
    }
}
