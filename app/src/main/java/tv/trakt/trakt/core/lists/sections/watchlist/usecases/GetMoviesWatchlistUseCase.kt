package tv.trakt.trakt.core.lists.sections.watchlist.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.core.lists.model.WatchlistItem
import tv.trakt.trakt.common.data.local.UserWatchlistLocalSource
import tv.trakt.trakt.common.data.remote.UserWatchlistRemoteSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.toInstant
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.common.model.ratings.UserRating
import tv.trakt.trakt.common.model.sorting.Sorting
import tv.trakt.trakt.common.model.toTraktId
import tv.trakt.trakt.core.lists.sections.watchlist.model.getWatchlistSorting

internal class GetMoviesWatchlistUseCase(
    private val remoteSource: UserWatchlistRemoteSource,
    private val userWatchlistLocalSource: UserWatchlistLocalSource,
) {
    suspend fun getLocalWatchlist(
        limit: Int,
        sort: Sorting,
    ): ImmutableList<WatchlistItem> {
        return userWatchlistLocalSource.getMovies()
            .sortedWith(getWatchlistSorting(sort))
            .take(limit)
            .toImmutableList()
    }

    suspend fun getRemoteWatchlist(
        page: Int,
        limit: Int,
        sorting: Sorting,
        filters: GlobalFilter,
        ratings: Map<TraktId, UserRating>? = null,
        skipLocal: Boolean = false,
    ): ImmutableList<WatchlistItem> {
        val response = remoteSource.getWatchlistMovies(
            page = page,
            limit = limit,
            sorting = sorting,
            extended = "full,images,colors",
            filters = filters,
        ).asyncMap {
            val mediaId = it.movie.ids.trakt.toTraktId()
            val listedAt = it.listedAt.toInstant()
            WatchlistItem.MovieItem(
                movie = Movie.fromDto(it.movie),
                rank = it.rank,
                listedAt = listedAt,
                userRating = ratings?.get(mediaId),
            )
        }

        return response
            .sortedWith(getWatchlistSorting(sorting))
            .toImmutableList()
            .also {
                if (skipLocal) return@also
                with(userWatchlistLocalSource) {
                    if (page == 1) {
                        setMovies(response)
                    } else {
                        addMovies(response)
                    }
                }
            }
    }
}
