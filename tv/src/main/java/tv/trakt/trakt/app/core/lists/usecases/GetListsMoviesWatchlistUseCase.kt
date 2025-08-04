package tv.trakt.trakt.app.core.lists.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.app.core.movies.data.local.MovieLocalDataSource
import tv.trakt.trakt.app.core.movies.model.Movie
import tv.trakt.trakt.app.core.movies.model.fromDto
import tv.trakt.trakt.app.core.sync.data.remote.movies.MoviesSyncRemoteDataSource
import tv.trakt.trakt.app.helpers.extensions.asyncMap

internal class GetListsMoviesWatchlistUseCase(
    private val remoteSyncSource: MoviesSyncRemoteDataSource,
    private val localMovieSource: MovieLocalDataSource,
) {
    suspend fun getMovies(
        limit: Int,
        page: Int? = null,
    ): ImmutableList<Movie> {
        val movies = remoteSyncSource.getWatchlist(
            page = page,
            limit = limit,
            extended = "full,cloud9,colors",
            sort = "added",
        ).sortedByDescending {
            it.listedAt
        }.asyncMap {
            Movie.fromDto(it.movie)
        }

        return movies
            .toImmutableList()
            .also {
                localMovieSource.upsertMovies(it)
            }
    }
}
