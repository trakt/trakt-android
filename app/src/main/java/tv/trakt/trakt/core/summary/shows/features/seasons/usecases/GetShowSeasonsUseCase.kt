package tv.trakt.trakt.core.summary.shows.features.seasons.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Season
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.core.discover.data.remote.ShowsRemoteDataSource
import tv.trakt.trakt.core.episodes.data.remote.EpisodesRemoteDataSource
import tv.trakt.trakt.core.summary.shows.features.seasons.model.EpisodeItem
import tv.trakt.trakt.core.summary.shows.features.seasons.model.ShowSeasons

internal class GetShowSeasonsUseCase(
    private val remoteShowsSource: ShowsRemoteDataSource,
    private val remoteEpisodesSource: EpisodesRemoteDataSource,
) {
    suspend fun getAllSeasons(
        showId: TraktId,
        initialSeason: Int,
    ): ShowSeasons {
        val remoteSeasons = remoteShowsSource.getSeasons(showId)
            .asyncMap { Season.fromDto(it) }
            .filter { (it.episodeCount ?: 0) > 0 }
            .sortedBy { it.number }

        val selectedSeason = remoteSeasons
            .firstOrNull {
                !it.isSpecial && (initialSeason == it.number)
            } ?: remoteSeasons.firstOrNull()

        if (selectedSeason != null) {
            val episodes = remoteEpisodesSource.getSeason(
                showId = showId,
                season = selectedSeason.number,
            ).asyncMap {
                EpisodeItem(
                    episode = Episode.fromDto(it),
                )
            }

            return ShowSeasons(
                seasons = remoteSeasons.toImmutableList(),
                selectedSeason = selectedSeason,
                selectedSeasonEpisodes = episodes.toImmutableList(),
            )
        }

        return ShowSeasons(
            seasons = remoteSeasons.toImmutableList(),
            selectedSeason = selectedSeason,
        )
    }

    suspend fun getSeasonEpisodes(
        showId: TraktId,
        season: Int,
    ): ImmutableList<EpisodeItem> {
        return remoteEpisodesSource.getSeason(showId, season)
            .asyncMap {
                EpisodeItem(
                    episode = Episode.fromDto(it),
                )
            }
            .toImmutableList()
    }
}
