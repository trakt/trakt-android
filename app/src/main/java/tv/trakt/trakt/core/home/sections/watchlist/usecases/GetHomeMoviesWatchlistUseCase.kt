package tv.trakt.trakt.core.home.sections.watchlist.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.core.lists.model.WatchlistItem
import tv.trakt.trakt.common.core.translations.usecase.GetLocalizedTitlesUseCase
import tv.trakt.trakt.common.data.local.HomeWatchlistLocalSource
import tv.trakt.trakt.common.data.remote.UserWatchlistRemoteSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.nowLocalDay
import tv.trakt.trakt.common.helpers.extensions.toInstant
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.common.model.sorting.SortOrder
import tv.trakt.trakt.common.model.sorting.SortType
import tv.trakt.trakt.common.model.sorting.Sorting

internal class GetHomeMoviesWatchlistUseCase(
    private val homeWatchlistLocalSource: HomeWatchlistLocalSource,
    private val userRemoteSource: UserWatchlistRemoteSource,
    private val getLocalizedTitlesUseCase: GetLocalizedTitlesUseCase,
) {
    suspend fun getLocalWatchlist(limit: Int): ImmutableList<WatchlistItem> {
        return homeWatchlistLocalSource.getMovieItems()
            .take(limit)
            .toImmutableList()
    }

    suspend fun getWatchlist(
        limit: Int,
        filters: GlobalFilter? = null,
        skipLocal: Boolean = false,
    ): ImmutableList<WatchlistItem> {
        val nowDay = nowLocalDay()
        return userRemoteSource.getWatchlistMovies(
            page = 1,
            limit = limit,
            extended = "full,colors",
            sorting = Sorting(
                type = SortType.Released,
                order = SortOrder.Desc,
            ),
            hide = "unreleased",
            filters = filters,
        )
            .asyncMap {
                WatchlistItem.MovieItem(
                    movie = Movie.fromDto(it.movie),
                    rank = it.rank,
                    listedAt = it.listedAt.toInstant(),
                )
            }
            .filter {
                it.movie.released != null &&
                    it.movie.released!! <= nowDay
            }
            .localized()
            .also {
                if (!skipLocal) {
                    homeWatchlistLocalSource.setMovieItems(items = it)
                }
            }
            .toImmutableList()
    }

    private suspend fun List<WatchlistItem.MovieItem>.localized(): List<WatchlistItem.MovieItem> {
        return getLocalizedTitlesUseCase.localizeMovies(
            items = this,
            movie = { it.movie },
            update = { item, movie -> item.copy(movie = movie) },
        )
    }
}
