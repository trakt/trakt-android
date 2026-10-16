package tv.trakt.trakt.core.summary.episodes.features.season.usecases

import tv.trakt.trakt.common.core.sync.model.ProgressItem
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.shows.data.remote.ShowsRemoteDataSource
import tv.trakt.trakt.core.summary.shows.features.seasons.model.EpisodeItem

internal class HasSkippedEpisodesUseCase(
    private val remoteShowsSource: ShowsRemoteDataSource,
) {
    // Specials excluded to match what GetWatchedUntilEpisodesUseCase marks.
    suspend fun hasSkippedEpisodes(
        showId: TraktId,
        episode: Episode,
        seasonEpisodes: List<EpisodeItem>,
        progress: ProgressItem.ShowItem?,
    ): Boolean {
        if (episode.season <= 0) return false

        val skippedInSeason = seasonEpisodes.any {
            it.episode.number < episode.number &&
                it.episode.isReleased &&
                !it.isWatched
        }
        if (skippedInSeason) return true
        if (episode.season == 1) return false

        val watchedBySeason = progress?.seasons.orEmpty()
            .associate { it.number to it.episodes.size }

        return remoteShowsSource.getSeasons(showId)
            .filter { it.number in 1 until episode.season }
            .any { (it.airedEpisodes ?: 0) > (watchedBySeason[it.number] ?: 0) }
    }
}
