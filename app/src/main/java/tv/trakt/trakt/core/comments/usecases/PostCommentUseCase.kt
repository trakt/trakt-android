package tv.trakt.trakt.core.comments.usecases

import tv.trakt.trakt.common.core.comments.data.remote.CommentsRemoteDataSource
import tv.trakt.trakt.common.model.Comment
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.MediaType.Episode
import tv.trakt.trakt.common.model.MediaType.Movie
import tv.trakt.trakt.common.model.MediaType.Season
import tv.trakt.trakt.common.model.MediaType.Show
import tv.trakt.trakt.common.model.TraktId

internal class PostCommentUseCase(
    private val remoteSource: CommentsRemoteDataSource,
) {
    suspend fun postComment(
        type: MediaType,
        mediaId: TraktId,
        comment: String,
        spoiler: Boolean,
    ): Comment {
        return when (type) {
            Show -> postShowComment(mediaId, comment, spoiler)
            Movie -> postMovieComment(mediaId, comment, spoiler)
            Season -> postSeasonComment(mediaId, comment, spoiler)
            Episode -> postEpisodeComment(mediaId, comment, spoiler)
        }
    }

    private suspend fun postShowComment(
        showId: TraktId,
        comment: String,
        spoiler: Boolean,
    ): Comment {
        return remoteSource.postShowComment(
            showId = showId,
            text = comment,
            spoiler = spoiler,
        ).let {
            Comment.fromDto(it)
        }
    }

    private suspend fun postMovieComment(
        movieId: TraktId,
        comment: String,
        spoiler: Boolean,
    ): Comment {
        return remoteSource.postMovieComment(
            movieId = movieId,
            text = comment,
            spoiler = spoiler,
        ).let {
            Comment.fromDto(it)
        }
    }

    private suspend fun postSeasonComment(
        seasonId: TraktId,
        comment: String,
        spoiler: Boolean,
    ): Comment {
        return remoteSource.postSeasonComment(
            seasonId = seasonId,
            text = comment,
            spoiler = spoiler,
        ).let {
            Comment.fromDto(it)
        }
    }

    private suspend fun postEpisodeComment(
        episodeId: TraktId,
        comment: String,
        spoiler: Boolean,
    ): Comment {
        return remoteSource.postEpisodeComment(
            episodeId = episodeId,
            text = comment,
            spoiler = spoiler,
        ).let {
            Comment.fromDto(it)
        }
    }
}
