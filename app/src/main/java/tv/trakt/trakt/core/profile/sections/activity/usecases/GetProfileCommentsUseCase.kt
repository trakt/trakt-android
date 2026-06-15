package tv.trakt.trakt.core.profile.sections.activity.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.core.user.data.remote.UserRemoteDataSource
import tv.trakt.trakt.common.model.Comment
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.model.pagination.Pagination
import tv.trakt.trakt.core.profile.sections.activity.data.local.comments.ProfileCommentsLocalDataSource
import tv.trakt.trakt.core.profile.sections.activity.model.ProfileCommentItem

internal class GetProfileCommentsUseCase(
    private val remoteSource: UserRemoteDataSource,
    private val localDataSource: ProfileCommentsLocalDataSource,
) {
    suspend fun getLocalComments(limit: Int): ImmutableList<ProfileCommentItem> {
        return localDataSource.getItems()
            .sortedByDescending { it.commentedAt }
            .take(limit)
            .toImmutableList()
    }

    suspend fun getRemoteComments(pagination: Pagination): ImmutableList<ProfileCommentItem> {
        val remoteItems = remoteSource.getComments(
            page = pagination.page,
            limit = pagination.limit,
        )

        return remoteItems
            .mapNotNull {
                when (it.type.value.lowercase()) {
                    "show" -> ProfileCommentItem.ShowItem(
                        show = Show.fromDto(it.show!!),
                        comment = Comment.fromDto(it.comment),
                    )
                    "movie" -> ProfileCommentItem.MovieItem(
                        movie = Movie.fromDto(it.movie!!),
                        comment = Comment.fromDto(it.comment),
                    )
                    "episode" -> ProfileCommentItem.EpisodeItem(
                        show = Show.fromDto(it.show!!),
                        episode = Episode.fromDto(it.episode!!),
                        comment = Comment.fromDto(it.comment),
                    )
                    else -> null
                }
            }
            .also {
                when (pagination.page) {
                    1 -> localDataSource.setItems(it)
                    else -> localDataSource.addItems(it)
                }
            }
            .toImmutableList()
    }
}
