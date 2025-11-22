package tv.trakt.trakt.core.comments.usecases

import tv.trakt.trakt.common.core.comments.data.remote.CommentsRemoteDataSource
import tv.trakt.trakt.common.model.Comment
import tv.trakt.trakt.common.model.TraktId

internal class PostCommentUseCase(
    private val remoteSource: CommentsRemoteDataSource,
) {
    suspend fun postShowComment(
        showId: TraktId,
        comment: String,
        spoiler: Boolean,
    ): Comment {
        val responseComment = remoteSource.postShowComment(
            showId = showId,
            text = comment,
            spoiler = spoiler,
        )
        return Comment.fromDto(responseComment)
    }

    suspend fun postMovieComment(
        movieId: TraktId,
        comment: String,
        spoiler: Boolean,
    ): Comment {
        val responseComment = remoteSource.postMovieComment(
            movieId = movieId,
            text = comment,
            spoiler = spoiler,
        )
        return Comment.fromDto(responseComment)
    }

    suspend fun postEpisodeComment(
        episodeId: TraktId,
        comment: String,
        spoiler: Boolean,
    ): Comment {
        val responseComment = remoteSource.postEpisodeComment(
            episodeId = episodeId,
            text = comment,
            spoiler = spoiler,
        )
        return Comment.fromDto(responseComment)
    }
}
