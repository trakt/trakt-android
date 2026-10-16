package tv.trakt.trakt.app.core.details.movie.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.core.movies.data.remote.MoviesRemoteSource
import tv.trakt.trakt.common.model.Comment
import tv.trakt.trakt.common.model.TraktId

internal class GetCommentsUseCase(
    private val remoteSource: MoviesRemoteSource,
) {
    suspend fun getComments(movieId: TraktId): ImmutableList<Comment> {
        val remoteComments = remoteSource.getComments(
            movieId = movieId,
            limit = 20,
            sort = "likes",
            extended = "images",
        )
            .map { Comment.fromDto(it) }

        return remoteComments.toImmutableList()
    }
}
