package tv.trakt.trakt.app.core.details.show.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.data.remote.ShowsRemoteSource
import tv.trakt.trakt.common.model.Comment
import tv.trakt.trakt.common.model.TraktId

internal class GetCommentsUseCase(
    private val remoteSource: ShowsRemoteSource,
) {
    suspend fun getComments(showId: TraktId): ImmutableList<Comment> {
        val remoteComments = remoteSource.getComments(
            showId = showId,
            limit = 20,
            sort = "likes",
            extended = "images",
        )
            .map { Comment.fromDto(it) }

        return remoteComments.toImmutableList()
    }
}
