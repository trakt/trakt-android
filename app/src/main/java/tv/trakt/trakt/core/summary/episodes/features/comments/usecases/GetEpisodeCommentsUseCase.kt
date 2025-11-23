package tv.trakt.trakt.core.summary.episodes.features.comments.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.Comment
import tv.trakt.trakt.common.model.SeasonEpisode
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.core.comments.model.CommentsFilter
import tv.trakt.trakt.core.comments.model.CommentsFilter.POPULAR
import tv.trakt.trakt.core.comments.model.CommentsFilter.RECENT
import tv.trakt.trakt.core.episodes.data.remote.EpisodesRemoteDataSource

internal class GetEpisodeCommentsUseCase(
    private val remoteSource: EpisodesRemoteDataSource,
) {
    suspend fun getComments(
        showId: TraktId,
        seasonEpisode: SeasonEpisode,
        user: User? = null,
        filter: CommentsFilter = POPULAR,
        limit: Int = 20,
    ): ImmutableList<Comment> {
        val remoteComments = remoteSource.getEpisodeComments(
            showId = showId,
            season = seasonEpisode.season,
            episode = seasonEpisode.episode,
            limit = limit,
            sort = when (filter) {
                POPULAR -> "likes"
                RECENT -> "newest"
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
