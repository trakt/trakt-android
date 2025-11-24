package tv.trakt.trakt.common.core.comments.data.remote

import org.openapitools.client.models.GetCommentsReactionsSummary200Response
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.networking.CommentDto

interface CommentsRemoteDataSource {
    suspend fun postShowComment(
        showId: TraktId,
        text: String,
        spoiler: Boolean,
    ): CommentDto

    suspend fun postMovieComment(
        movieId: TraktId,
        text: String,
        spoiler: Boolean,
    ): CommentDto

    suspend fun postEpisodeComment(
        episodeId: TraktId,
        text: String,
        spoiler: Boolean,
    ): CommentDto

    suspend fun deleteComment(commentId: TraktId)

    suspend fun getCommentReplies(commentId: Int): List<CommentDto>

    suspend fun getCommentReactions(commentId: Int): GetCommentsReactionsSummary200Response
}
