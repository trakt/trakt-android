package tv.trakt.app.tv.core.details.episode.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.app.tv.common.model.Comment
import tv.trakt.app.tv.common.model.SeasonEpisode
import tv.trakt.app.tv.common.model.TraktId
import tv.trakt.app.tv.core.episodes.data.remote.EpisodesRemoteDataSource

internal class GetCommentsUseCase(
    private val remoteSource: EpisodesRemoteDataSource,
) {
    suspend fun getComments(
        showId: TraktId,
        seasonEpisode: SeasonEpisode,
    ): ImmutableList<Comment> {
        val remoteComments = remoteSource.getEpisodeComments(
            showId,
            season = seasonEpisode.season,
            episode = seasonEpisode.episode,
        )
            .map { Comment.fromDto(it) }

        return remoteComments.toImmutableList()
    }
}
