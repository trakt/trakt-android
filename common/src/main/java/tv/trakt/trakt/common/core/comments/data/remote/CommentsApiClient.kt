package tv.trakt.trakt.common.core.comments.data.remote

import org.openapitools.client.apis.CommentsApi
import org.openapitools.client.models.GetCommentsReactionsSummary200Response
import org.openapitools.client.models.PostCommentsPostRequest
import org.openapitools.client.models.PostCommentsPostRequestAllOfOneOfMovie
import org.openapitools.client.models.PostCommentsPostRequestAllOfOneOfMovieIds
import org.openapitools.client.models.PostCommentsReplyRequest
import org.openapitools.client.models.PostCommentsReportRequest
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.networking.CommentDto
import tv.trakt.trakt.common.networking.helpers.CacheMarkerProvider

class CommentsApiClient(
    private val api: CommentsApi,
    private val authorizedApi: CommentsApi,
    private val cacheMarker: CacheMarkerProvider,
) : CommentsRemoteDataSource {
    override suspend fun postShowComment(
        showId: TraktId,
        text: String,
        spoiler: Boolean,
    ): CommentDto {
        val request = PostCommentsPostRequest(
            comment = text,
            spoiler = spoiler,
            show = PostCommentsPostRequestAllOfOneOfMovie(
                ids = PostCommentsPostRequestAllOfOneOfMovieIds(
                    trakt = showId.value,
                ),
            ),
        )

        val result = authorizedApi.postCommentsPost(request)
        cacheMarker.invalidate()

        return result.body()
    }

    override suspend fun postMovieComment(
        movieId: TraktId,
        text: String,
        spoiler: Boolean,
    ): CommentDto {
        val request = PostCommentsPostRequest(
            comment = text,
            spoiler = spoiler,
            movie = PostCommentsPostRequestAllOfOneOfMovie(
                ids = PostCommentsPostRequestAllOfOneOfMovieIds(
                    trakt = movieId.value,
                ),
            ),
        )

        val result = authorizedApi.postCommentsPost(request)
        cacheMarker.invalidate()

        return result.body()
    }

    override suspend fun postSeasonComment(
        seasonId: TraktId,
        text: String,
        spoiler: Boolean,
    ): CommentDto {
        val request = PostCommentsPostRequest(
            comment = text,
            spoiler = spoiler,
            season = PostCommentsPostRequestAllOfOneOfMovie(
                ids = PostCommentsPostRequestAllOfOneOfMovieIds(
                    trakt = seasonId.value,
                ),
            ),
        )

        val result = authorizedApi.postCommentsPost(request)
        cacheMarker.invalidate()

        return result.body()
    }

    override suspend fun postEpisodeComment(
        episodeId: TraktId,
        text: String,
        spoiler: Boolean,
    ): CommentDto {
        val request = PostCommentsPostRequest(
            comment = text,
            spoiler = spoiler,
            episode = PostCommentsPostRequestAllOfOneOfMovie(
                ids = PostCommentsPostRequestAllOfOneOfMovieIds(
                    trakt = episodeId.value,
                ),
            ),
        )

        val result = authorizedApi.postCommentsPost(request)
        cacheMarker.invalidate()

        return result.body()
    }

    override suspend fun postReply(
        commentId: TraktId,
        text: String,
        spoiler: Boolean,
    ): CommentDto {
        val request = PostCommentsReplyRequest(
            comment = text,
            spoiler = spoiler,
        )

        val result = authorizedApi.postCommentsReply(
            id = commentId.value.toString(),
            postCommentsReplyRequest = request,
        )
        cacheMarker.invalidate()

        return result.body()
    }

    override suspend fun postReport(
        commentId: Int,
        reason: String,
        message: String,
    ) {
        val request = PostCommentsReportRequest(
            reason = PostCommentsReportRequest.Reason.entries.first { it.value == reason },
            message = message.ifBlank { null },
        )

        authorizedApi.postCommentsReport(
            id = commentId.toString(),
            postCommentsReportRequest = request,
        )
        cacheMarker.invalidate()
    }

    override suspend fun deleteComment(commentId: TraktId) {
        authorizedApi.deleteCommentsDelete(commentId.value.toString())
        cacheMarker.invalidate()
    }

    override suspend fun getCommentReplies(commentId: Int): List<CommentDto> {
        val response = api.getCommentsReplies(
            id = commentId.toString(),
            page = null,
            limit = 99,
            extended = "images",
        )

        return response.body()
    }

    override suspend fun getCommentReactions(commentId: Int): GetCommentsReactionsSummary200Response {
        val response = api.getCommentsReactionsSummary(
            id = commentId.toString(),
        )

        return response.body()
    }
}
