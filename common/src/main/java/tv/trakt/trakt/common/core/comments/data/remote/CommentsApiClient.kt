package tv.trakt.trakt.common.core.comments.data.remote

import org.openapitools.client.apis.CommentsApi
import org.openapitools.client.models.GetCommentsReactionsSummary200Response
import org.openapitools.client.models.PostCommentsPostRequest
import org.openapitools.client.models.PostCommentsPostRequestAllOfOneOfMovie
import org.openapitools.client.models.PostCommentsPostRequestAllOfOneOfMovieIds
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.networking.CommentDto
import tv.trakt.trakt.common.networking.helpers.CacheMarkerProvider

class CommentsApiClient(
    private val api: CommentsApi,
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

        val result = api.postCommentsPost(request)
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

        val result = api.postCommentsPost(request)
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

        val result = api.postCommentsPost(request)
        cacheMarker.invalidate()

        return result.body()
    }

    override suspend fun deleteComment(commentId: TraktId) {
        api.deleteCommentsDelete(commentId.value.toString())
        cacheMarker.invalidate()
    }

    override suspend fun getCommentReplies(commentId: Int): List<CommentDto> {
        val response = api.getCommentsReplies(
            id = commentId.toString(),
            page = null,
            limit = 99,
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
