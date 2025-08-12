package tv.trakt.trakt.app.core.search.usecase

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import tv.trakt.trakt.app.core.search.data.remote.SearchRemoteDataSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.fromDto

internal class GetSearchResultsUseCase(
    private val remoteSource: SearchRemoteDataSource,
) {
    suspend fun getSearchResults(query: String): Result {
        if (query.trim().isBlank()) {
            return Result()
        }
        return coroutineScope {
            val showsAsync = async { remoteSource.getShows(query, 30) }
            val moviesAsync = async { remoteSource.getMovies(query, 30) }

            val shows = showsAsync.await()
            val movies = moviesAsync.await()

            Result(
                shows = shows
                    .asyncMap { Show.fromDto(it.show!!) }
                    .toImmutableList(),
                movies = movies
                    .asyncMap { Movie.fromDto(it.movie!!) }
                    .toImmutableList(),
            )
        }
    }

    data class Result(
        val shows: ImmutableList<Show> = listOf<Show>().toImmutableList(),
        val movies: ImmutableList<Movie> = listOf<Movie>().toImmutableList(),
    )
}
