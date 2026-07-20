package tv.trakt.trakt.app.core.details.show.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.app.core.details.show.models.ShowSeasons
import tv.trakt.trakt.app.core.episodes.data.remote.EpisodesRemoteDataSource
import tv.trakt.trakt.app.core.shows.data.remote.ShowsRemoteDataSource
import tv.trakt.trakt.common.core.episodes.data.local.EpisodeLocalDataSource
import tv.trakt.trakt.common.core.sync.model.ProgressItem.ShowItem
import tv.trakt.trakt.common.core.user.usecases.progress.LoadUserProgressUseCase
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Season
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.fromDto

internal class GetShowSeasonsUseCase(
    private val remoteShowsSource: ShowsRemoteDataSource,
    private val remoteEpisodesSource: EpisodesRemoteDataSource,
    private val localEpisodeSource: EpisodeLocalDataSource,
    private val loadUserProgressUseCase: LoadUserProgressUseCase,
) {
    suspend fun getAllSeasons(showId: TraktId): ShowSeasons {
        val remoteSeasons = remoteShowsSource.getShowSeasons(showId)
            .asyncMap { Season.fromDto(it) }
            .filter { (it.episodeCount ?: 0) > 0 }
            .sortedBy { it.number }

        val progress = when (loadUserProgressUseCase.isShowsLoaded()) {
            true -> loadUserProgressUseCase.loadLocalShows()
            false -> loadUserProgressUseCase.loadShowsProgress(notifyUpdate = false)
        }.firstOrNull {
            it.showId == showId
        }

        val mappedSeasons = remoteSeasons
            .map {
                ShowSeasons.SeasonItem(
                    season = it,
                    watching = isSeasonWatching(it, progress?.seasons),
                    watched = isSeasonWatched(it, progress?.seasons),
                )
            }
            .toImmutableList()

        val selectedSeason = mappedSeasons
            .firstOrNull { !it.season.isSpecial }
            ?: mappedSeasons.firstOrNull()

        if (selectedSeason != null) {
            val episodes = remoteEpisodesSource
                .getEpisodeSeason(showId, selectedSeason.season.number)
                .asyncMap {
                    val episode = Episode.fromDto(it)
                    ShowSeasons.EpisodeItem(
                        episode = episode,
                        watched = progress?.isEpisodeWatched(
                            selectedSeason.season.number,
                            episode.ids.trakt,
                        ) == true,
                    )
                }

            return ShowSeasons(
                seasons = mappedSeasons,
                selectedSeason = selectedSeason,
                selectedSeasonEpisodes = episodes.toImmutableList(),
            ).also {
                localEpisodeSource.upsertEpisodes(episodes.map { it.episode })
            }
        }

        return ShowSeasons(
            seasons = mappedSeasons.toImmutableList(),
            selectedSeason = selectedSeason,
        )
    }

    suspend fun getSeason(
        showId: TraktId,
        season: Int,
    ): ImmutableList<ShowSeasons.EpisodeItem> {
        val progress = when (loadUserProgressUseCase.isShowsLoaded()) {
            true -> loadUserProgressUseCase.loadLocalShows()
            false -> loadUserProgressUseCase.loadShowsProgress(notifyUpdate = false)
        }.firstOrNull {
            it.showId == showId
        }

        return remoteEpisodesSource.getEpisodeSeason(showId, season)
            .asyncMap {
                val episode = Episode.fromDto(it)
                ShowSeasons.EpisodeItem(
                    episode = episode,
                    watched = progress?.isEpisodeWatched(
                        seasonNumber = season,
                        episodeId = episode.ids.trakt,
                    ) == true,
                )
            }
            .toImmutableList()
            .also {
                localEpisodeSource.upsertEpisodes(it.map { item -> item.episode })
            }
    }

    private fun isSeasonWatched(
        season: Season,
        progress: ImmutableList<ShowItem.Season>?,
    ): Boolean {
        val total = season.episodeCount ?: 0
        return total > 0 && watchedEpisodeCount(season, progress) == total
    }

    private fun isSeasonWatching(
        season: Season,
        progress: ImmutableList<ShowItem.Season>?,
    ): Boolean {
        val total = season.episodeCount ?: 0
        return watchedEpisodeCount(season, progress) in 1 until total
    }

    private fun watchedEpisodeCount(
        season: Season,
        progress: ImmutableList<ShowItem.Season>?,
    ): Int {
        return progress
            ?.firstOrNull { it.number == season.number }
            ?.episodes
            ?.count { it.plays.isNotEmpty() }
            ?: 0
    }
}
