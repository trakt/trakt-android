package tv.trakt.trakt.core.summary.shows.features.progress.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.core.sync.model.ProgressItem.ShowItem
import tv.trakt.trakt.common.core.user.usecases.progress.LoadUserProgressUseCase
import tv.trakt.trakt.common.data.remote.EpisodesRemoteSource
import tv.trakt.trakt.common.data.remote.ShowsRemoteSource
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Season
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.model.toTraktId
import tv.trakt.trakt.core.summary.shows.features.progress.ShowProgressState.UpNext

internal class GetShowProgressUseCase(
    private val remoteShowsSource: ShowsRemoteSource,
    private val remoteEpisodesSource: EpisodesRemoteSource,
    private val loadUserProgressUseCase: LoadUserProgressUseCase,
) {
    suspend fun getSeasons(showId: TraktId): List<Season> {
        return remoteShowsSource.getSeasons(showId)
            .map { Season.fromDto(it) }
            .filter { (it.episodeCount ?: 0) > 0 }
    }

    suspend fun getSeasonEpisodes(
        showId: TraktId,
        season: Int,
    ): ImmutableList<Episode> {
        return remoteEpisodesSource.getSeason(showId, season)
            .map { Episode.fromDto(it) }
            .toImmutableList()
    }

    suspend fun getUpNext(showId: TraktId): UpNext? {
        val progress = remoteShowsSource.getWatchedProgress(showId)
        val next = progress.nextEpisode ?: return null
        if (progress.aired - progress.completed <= 0) return null

        return UpNext(
            episodeId = next.ids.trakt.toTraktId(),
            season = next.season,
        )
    }

    suspend fun getUserProgress(
        showId: TraktId,
        refresh: Boolean,
    ): ShowItem? {
        val shows = when {
            !refresh && loadUserProgressUseCase.isShowsLoaded() -> loadUserProgressUseCase.loadLocalShows()
            else -> loadUserProgressUseCase.loadShowsProgress()
        }
        return shows.firstOrNull { it.showId == showId }
    }
}
