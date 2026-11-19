package tv.trakt.trakt.core.summary.episodes.features.season.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.core.translations.usecase.GetLocalizedTitlesUseCase
import tv.trakt.trakt.common.data.remote.EpisodesRemoteSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.core.summary.shows.features.seasons.model.EpisodeItem

internal class GetEpisodeSeasonUseCase(
    private val remoteEpisodesSource: EpisodesRemoteSource,
    private val getLocalizedTitlesUseCase: GetLocalizedTitlesUseCase,
) {
    suspend fun getSeasonEpisodes(
        showId: TraktId,
        seasonNumber: Int,
    ): ImmutableList<EpisodeItem> {
        return remoteEpisodesSource.getSeason(showId, seasonNumber)
            .asyncMap {
                EpisodeItem(
                    episode = Episode.fromDto(it),
                )
            }
            .localized()
            .toImmutableList()
    }

    private suspend fun List<EpisodeItem>.localized(): List<EpisodeItem> {
        return getLocalizedTitlesUseCase.localizeEpisodes(
            items = this,
            episode = { it.episode },
            update = { item, episode -> item.copy(episode = episode) },
        )
    }
}
