package tv.trakt.trakt.core.summary.movies.features.comments.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.Comment
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.movies.data.remote.MoviesRemoteDataSource

internal class GetMovieCommentsUseCase(
    private val remoteSource: MoviesRemoteDataSource,
) {
    suspend fun getComments(movieId: TraktId): ImmutableList<Comment> {
        val remoteComments = remoteSource.getComments(
            movieId = movieId,
            limit = 30,
        ).asyncMap {
            Comment.Companion.fromDto(it)
        }

        return remoteComments
            .toImmutableList()
    }
}
