package tv.trakt.trakt.app.core.details.show.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.app.core.details.show.models.ShowSeasons
import tv.trakt.trakt.app.core.episodes.data.local.EpisodeLocalDataSource
import tv.trakt.trakt.app.core.episodes.data.remote.EpisodesRemoteDataSource
import tv.trakt.trakt.app.core.shows.data.remote.ShowsRemoteDataSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Season
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.fromDto

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
