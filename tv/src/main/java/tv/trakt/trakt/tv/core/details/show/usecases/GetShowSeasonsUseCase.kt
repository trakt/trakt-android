package tv.trakt.trakt.tv.core.details.show.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.tv.common.model.TraktId
import tv.trakt.trakt.tv.core.details.show.models.ShowSeasons
import tv.trakt.trakt.tv.core.episodes.data.local.EpisodeLocalDataSource
import tv.trakt.trakt.tv.core.episodes.data.remote.EpisodesRemoteDataSource
import tv.trakt.trakt.tv.core.episodes.model.Episode
import tv.trakt.trakt.tv.core.episodes.model.Season
import tv.trakt.trakt.tv.core.episodes.model.fromDto
import tv.trakt.trakt.tv.core.shows.data.remote.ShowsRemoteDataSource
import tv.trakt.trakt.tv.helpers.extensions.asyncMap

internal class GetShowSeasonsUseCase(
    private val remoteShowsSource: ShowsRemoteDataSource,
    private val remoteEpisodesSource: EpisodesRemoteDataSource,
    private val localEpisodeSource: EpisodeLocalDataSource,
) {
    suspend fun getAllSeasons(showId: TraktId): ShowSeasons {
        val remoteSeasons = remoteShowsSource.getShowSeasons(showId)
            .asyncMap { Season.fromDto(it) }
            .filter { (it.episodeCount ?: 0) > 0 }
            .sortedBy { it.number }

        val selectedSeason = remoteSeasons
            .firstOrNull { !it.isSpecial }
            ?: remoteSeasons.firstOrNull()

        if (selectedSeason != null) {
            val episodes = remoteEpisodesSource.getEpisodeSeason(showId, selectedSeason.number)
                .asyncMap { Episode.fromDto(it) }

            return ShowSeasons(
                seasons = remoteSeasons.toImmutableList(),
                selectedSeason = selectedSeason,
                selectedSeasonEpisodes = episodes.toImmutableList(),
            ).also {
                localEpisodeSource.upsertEpisodes(episodes)
            }
        }

        return ShowSeasons(
            seasons = remoteSeasons.toImmutableList(),
            selectedSeason = selectedSeason,
        )
    }

    suspend fun getSeason(
        showId: TraktId,
        season: Int,
    ): ImmutableList<Episode> {
        return remoteEpisodesSource.getEpisodeSeason(showId, season)
            .asyncMap { Episode.fromDto(it) }
            .toImmutableList()
            .also {
                localEpisodeSource.upsertEpisodes(it)
            }
    }
}
