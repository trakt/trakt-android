package tv.trakt.trakt.core.summary.movies.features.related.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.data.local.MovieLocalSource
import tv.trakt.trakt.common.data.remote.MoviesRemoteSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.fromDto

internal class GetMovieRelatedUseCase(
    private val remoteSource: MoviesRemoteSource,
    private val localSource: MovieLocalSource,
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
