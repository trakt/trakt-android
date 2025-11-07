package tv.trakt.trakt.core.summary.shows.features.comments.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.Comment
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.comments.model.CommentsFilter
import tv.trakt.trakt.core.comments.model.CommentsFilter.POPULAR
import tv.trakt.trakt.core.comments.model.CommentsFilter.RECENT
import tv.trakt.trakt.core.discover.data.remote.ShowsRemoteDataSource

internal class GetShowCommentsUseCase(
    private val remoteSource: ShowsRemoteDataSource,
) {
    suspend fun getComments(
        showId: TraktId,
        filter: CommentsFilter = POPULAR,
        limit: Int = 20,
    ): ImmutableList<Comment> {
        val remoteComments = remoteSource.getComments(
            showId = showId,
            limit = limit,
            sort = when (filter) {
                POPULAR -> "likes"
                RECENT -> "newest"
            },
        ).asyncMap {
            Comment.fromDto(it)
        }

        return remoteComments
            .toImmutableList()
    }
}
