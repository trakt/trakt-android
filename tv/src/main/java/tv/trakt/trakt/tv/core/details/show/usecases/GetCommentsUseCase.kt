package tv.trakt.trakt.tv.core.details.show.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.tv.common.model.Comment
import tv.trakt.trakt.tv.common.model.TraktId
import tv.trakt.trakt.tv.core.shows.data.remote.ShowsRemoteDataSource

internal class GetCommentsUseCase(
    private val remoteSource: ShowsRemoteDataSource,
) {
    suspend fun getComments(showId: TraktId): ImmutableList<Comment> {
        val remoteComments = remoteSource.getShowComments(showId)
            .map { Comment.fromDto(it) }

        return remoteComments.toImmutableList()
    }
}
