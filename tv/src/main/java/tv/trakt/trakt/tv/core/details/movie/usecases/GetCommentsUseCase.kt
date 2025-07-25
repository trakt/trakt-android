package tv.trakt.trakt.tv.core.details.movie.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.tv.common.model.Comment
import tv.trakt.trakt.tv.core.movies.data.remote.MoviesRemoteDataSource

internal class GetCommentsUseCase(
    private val remoteSource: MoviesRemoteDataSource,
) {
    suspend fun getComments(movieId: TraktId): ImmutableList<Comment> {
        val remoteComments = remoteSource.getMovieComments(movieId)
            .map { Comment.fromDto(it) }

        return remoteComments.toImmutableList()
    }
}
