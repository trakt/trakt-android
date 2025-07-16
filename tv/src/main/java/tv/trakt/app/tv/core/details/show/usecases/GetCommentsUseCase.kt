package tv.trakt.app.tv.core.details.show.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.app.tv.common.model.Comment
import tv.trakt.app.tv.common.model.TraktId
import tv.trakt.app.tv.core.shows.data.remote.ShowsRemoteDataSource

internal class GetCommentsUseCase(
    private val remoteSource: ShowsRemoteDataSource,
) {
    suspend fun getComments(showId: TraktId): ImmutableList<Comment> {
        val remoteComments = remoteSource.getShowComments(showId)
            .map { Comment.fromDto(it) }

        return remoteComments.toImmutableList()
    }
}
