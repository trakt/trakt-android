package tv.trakt.trakt.app.core.home.sections.startwatching.usecases

import tv.trakt.trakt.app.core.home.sections.startwatching.model.WatchlistItem
import tv.trakt.trakt.app.core.sync.data.remote.movies.MoviesSyncRemoteDataSource
import tv.trakt.trakt.common.core.movies.data.local.MovieLocalDataSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.nowLocalDay
import tv.trakt.trakt.common.helpers.extensions.toZonedDateTime
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.model.pagination.Pagination

private val SortComparator =
    compareByDescending<WatchlistItem> { it.released }
        .thenByDescending { it.listedAt }

internal class GetHomeMoviesWatchlistItemsUseCase(
    private val remoteSyncSource: MoviesSyncRemoteDataSource,
    private val localMovieSource: MovieLocalDataSource,
) {
    suspend fun getItems(pagination: Pagination): List<WatchlistItem> {
        val nowDay = nowLocalDay().toString()

        val response = remoteSyncSource.getWatchlist(
            page = pagination.page,
            limit = pagination.limit,
            extended = "full,cloud9,colors,streaming_ids",
            sort = "released",
            hide = "unreleased",
        ).filter {
            !it.movie.released.isNullOrBlank() && it.movie.released!! <= nowDay
        }.asyncMap {
            WatchlistItem.MovieItem(
                movie = Movie.fromDto(it.movie),
                rank = it.rank,
                listedAt = it.listedAt.toZonedDateTime(),
            )
        }.sortedWith(SortComparator)

        return response
            .also {
                val movies = response.asyncMap { it.movie }
                localMovieSource.upsertMovies(movies)
            }
    }
}
