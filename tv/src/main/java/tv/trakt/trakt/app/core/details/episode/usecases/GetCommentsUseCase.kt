package tv.trakt.trakt.app.core.details.episode.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.core.episodes.data.remote.EpisodesRemoteSource
import tv.trakt.trakt.common.model.Comment
import tv.trakt.trakt.common.model.SeasonEpisode
import tv.trakt.trakt.common.model.TraktId

internal class GetCommentsUseCase(
    private val remoteSource: EpisodesRemoteSource,
) {
    suspend fun getComments(
        showId: TraktId,
        seasonEpisode: SeasonEpisode,
    ): ImmutableList<Comment> {
        val remoteComments = remoteSource.getEpisodeComments(
            showId,
            season = seasonEpisode.season,
            episode = seasonEpisode.episode,
            extended = "images",
        )
            .map { Comment.fromDto(it) }

        return remoteComments.toImmutableList()
    }
}
