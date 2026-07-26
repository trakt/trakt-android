package tv.trakt.trakt.app.core.lists.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.app.core.lists.filters.TvListPage
import tv.trakt.trakt.app.core.lists.filters.TvListRequest
import tv.trakt.trakt.common.core.movies.data.local.MovieLocalDataSource
import tv.trakt.trakt.common.core.user.data.remote.watchlist.UserWatchlistRemoteDataSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.fromDto

internal class GetListsMoviesWatchlistUseCase(
    private val remoteSource: UserWatchlistRemoteDataSource,
    private val localMovieSource: MovieLocalDataSource,
) {
    suspend fun getMovies(request: TvListRequest): TvListPage<Movie> {
        val response = remoteSource.getWatchlistMovies(
            page = request.page,
            limit = request.limit,
            extended = "full,cloud9,colors,streaming_ids",
            sorting = request.sorting,
            filters = request.filter,
        )
        val movies: ImmutableList<Movie> = response.asyncMap {
            Movie.fromDto(it.movie)
        }.toImmutableList()

        localMovieSource.upsertMovies(movies)

        return TvListPage(
            items = movies,
            nextPage = request.page + 1,
            hasMore = response.size >= request.limit,
        )
    }
}
