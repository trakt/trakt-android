package tv.trakt.trakt.core.summary.shows.features.seasons.all.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.Comment
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.core.comments.model.CommentsFilter
import tv.trakt.trakt.core.comments.model.CommentsFilter.Popular
import tv.trakt.trakt.core.comments.model.CommentsFilter.Recent
import tv.trakt.trakt.core.episodes.data.remote.EpisodesRemoteDataSource

internal class GetSeasonCommentsUseCase(
    private val remoteSource: EpisodesRemoteDataSource,
) {
    suspend fun getComments(
        showId: TraktId,
        season: Int,
        user: User? = null,
        filter: CommentsFilter = Popular,
        limit: Int = 20,
    ): ImmutableList<Comment> {
        val remoteComments = remoteSource.getSeasonComments(
            showId = showId,
            season = season,
            limit = limit,
            sort = when (filter) {
                Popular -> "likes"
                Recent -> "newest"
            },
        ).asyncMap {
            Comment.fromDto(it)
        }

        return remoteComments
            .sortedByDescending { comment ->
                user?.let { comment.user.ids.slug == it.ids.slug } ?: false
            }
            .toImmutableList()
    }
}
