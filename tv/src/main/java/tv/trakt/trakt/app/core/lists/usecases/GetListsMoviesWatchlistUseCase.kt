package tv.trakt.trakt.app.core.lists.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.core.media.movies.local.MovieLocalDataSource
import tv.trakt.trakt.common.core.sync.data.remote.movies.MoviesSyncRemoteSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.model.sorting.Sorting

internal class GetListsMoviesWatchlistUseCase(
    private val remoteSyncSource: MoviesSyncRemoteSource,
    private val localMovieSource: MovieLocalDataSource,
) {
    suspend fun getMovies(
        limit: Int,
        page: Int? = null,
    ): ImmutableList<Movie> {
        val movies = remoteSyncSource.getWatchlist(
            page = page,
            limit = limit,
            extended = "full,images,colors,streaming_ids",
            sorting = Sorting.RecentlyAdded,
        ).asyncMap {
            Movie.fromDto(it.movie)
        }

        return movies
            .toImmutableList()
            .also {
                localMovieSource.upsertMovies(it)
            }
    }
}
