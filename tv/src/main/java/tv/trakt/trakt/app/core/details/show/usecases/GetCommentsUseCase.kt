package tv.trakt.trakt.app.core.details.show.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.app.common.model.Comment
import tv.trakt.trakt.app.core.shows.data.remote.ShowsRemoteDataSource
import tv.trakt.trakt.common.model.TraktId

internal class GetCommentsUseCase(
    private val remoteSource: ShowsRemoteDataSource,
) {
    suspend fun getComments(showId: TraktId): ImmutableList<Comment> {
        val remoteComments = remoteSource.getShowComments(showId)
            .map { Comment.fromDto(it) }

        return remoteComments.toImmutableList()
    }
}
