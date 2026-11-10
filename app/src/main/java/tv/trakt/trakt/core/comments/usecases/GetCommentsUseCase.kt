package tv.trakt.trakt.core.comments.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.data.remote.EpisodesRemoteSource
import tv.trakt.trakt.common.data.remote.MoviesRemoteSource
import tv.trakt.trakt.common.data.remote.ShowsRemoteSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.Comment
import tv.trakt.trakt.common.model.SeasonEpisode
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.core.comments.model.CommentsFilter
import tv.trakt.trakt.core.comments.model.CommentsFilter.Popular
import tv.trakt.trakt.core.comments.model.CommentsFilter.Recent

internal class GetCommentsUseCase(
    private val moviesRemoteSource: MoviesRemoteSource,
    private val showsRemoteSource: ShowsRemoteSource,
    private val episodesRemoteSource: EpisodesRemoteSource,
) {
    suspend fun getMovieComments(
        movieId: TraktId,
        user: User? = null,
        filter: CommentsFilter = Popular,
        language: String? = null,
        limit: Int = 20,
    ): ImmutableList<Comment> {
        return moviesRemoteSource.getComments(
            movieId = movieId,
            limit = limit,
            sort = filter.sort,
            language = language,
        ).asyncMap {
            Comment.fromDto(it)
        }.sortedWithOwnFirst(user)
    }

    suspend fun getShowComments(
        showId: TraktId,
        user: User? = null,
        filter: CommentsFilter = Popular,
        language: String? = null,
        limit: Int = 20,
    ): ImmutableList<Comment> {
        return showsRemoteSource.getComments(
            showId = showId,
            limit = limit,
            sort = filter.sort,
            language = language,
        ).asyncMap {
            Comment.fromDto(it)
        }.sortedWithOwnFirst(user)
    }

    suspend fun getEpisodeComments(
        showId: TraktId,
        seasonEpisode: SeasonEpisode,
        user: User? = null,
        filter: CommentsFilter = Popular,
        language: String? = null,
        limit: Int = 20,
    ): ImmutableList<Comment> {
        return episodesRemoteSource.getEpisodeComments(
            showId = showId,
            season = seasonEpisode.season,
            episode = seasonEpisode.episode,
            limit = limit,
            sort = filter.sort,
            language = language,
        ).asyncMap {
            Comment.fromDto(it)
        }.sortedWithOwnFirst(user)
    }

    private val CommentsFilter.sort: String
        get() = when (this) {
            Popular -> "likes"
            Recent -> "newest"
        }

    private fun List<Comment>.sortedWithOwnFirst(user: User?): ImmutableList<Comment> {
        return sortedByDescending { comment ->
            user?.let { comment.user.ids.slug == it.ids.slug } ?: false
        }.toImmutableList()
    }
}
