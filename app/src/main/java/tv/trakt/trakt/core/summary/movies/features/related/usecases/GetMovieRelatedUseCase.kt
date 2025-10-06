package tv.trakt.trakt.core.summary.movies.features.related.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.core.movies.data.local.MovieLocalDataSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.core.movies.data.remote.MoviesRemoteDataSource

internal class GetMovieRelatedUseCase(
    private val remoteSource: MoviesRemoteDataSource,
    private val localSource: MovieLocalDataSource,
) {
    suspend fun getRelatedMovies(movieId: TraktId): ImmutableList<Movie> {
        val relatedMovies = remoteSource.getRelated(movieId)

        val movies = relatedMovies
            .take(30)
            .asyncMap { Movie.fromDto(it) }

        localSource.upsertMovies(movies)

        return movies.toImmutableList()
    }
}
