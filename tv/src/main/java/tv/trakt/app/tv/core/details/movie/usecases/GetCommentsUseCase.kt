package tv.trakt.app.tv.core.details.movie.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.app.tv.common.model.Comment
import tv.trakt.app.tv.common.model.TraktId
import tv.trakt.app.tv.core.movies.data.remote.MoviesRemoteDataSource

internal class GetCommentsUseCase(
    private val remoteSource: MoviesRemoteDataSource,
) {
    suspend fun getComments(movieId: TraktId): ImmutableList<Comment> {
        val remoteComments = remoteSource.getMovieComments(movieId)
            .map { Comment.fromDto(it) }

        return remoteComments.toImmutableList()
    }
}
