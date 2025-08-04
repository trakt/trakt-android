package tv.trakt.trakt.app.core.details.movie.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.app.common.model.Comment
import tv.trakt.trakt.app.core.movies.data.remote.MoviesRemoteDataSource
import tv.trakt.trakt.common.model.TraktId

internal class GetCommentsUseCase(
    private val remoteSource: MoviesRemoteDataSource,
) {
    suspend fun getComments(movieId: TraktId): ImmutableList<Comment> {
        val remoteComments = remoteSource.getMovieComments(movieId)
            .map { Comment.fromDto(it) }

        return remoteComments.toImmutableList()
    }
}
