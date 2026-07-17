package tv.trakt.trakt.core.summary.shows.features.seasons.watcheduntil.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import tv.trakt.trakt.common.core.user.data.remote.history.UserHistoryRemoteDataSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.model.pagination.Pagination
import tv.trakt.trakt.core.episodes.data.remote.EpisodesRemoteDataSource
import tv.trakt.trakt.core.shows.data.remote.ShowsRemoteDataSource

internal class GetWatchedUntilEpisodesUseCase(
    private val remoteShowsSource: ShowsRemoteDataSource,
    private val remoteEpisodesSource: EpisodesRemoteDataSource,
    private val remoteHistorySource: UserHistoryRemoteDataSource,
) {
    suspend fun getEpisodes(
        show: Show,
        selectedEpisode: Episode,
    ): ImmutableList<Episode> =
        coroutineScope {
            val historyIdsDeferred = async { getHistoryIds(show) }
            val episodesDeferred = async { getAllEpisodes(show, selectedEpisode) }

            val historyIds = historyIdsDeferred.await()
            val episodes = episodesDeferred.await()

            episodes
                .filter {
                    val se = it.seasonEpisode
                    se.id <= selectedEpisode.seasonEpisode.id && !historyIds.contains(se.id)
                }
                .sortedBy { it.seasonEpisode.id }
                .toImmutableList()
        }

    private suspend fun getHistoryIds(show: Show): Set<Long> {
        return remoteHistorySource.getShowHistory(
            showId = show.ids.trakt,
            pagination = Pagination(page = 1, limit = 250),
        ).asyncMap {
            Episode.fromDto(
                checkNotNull(it.episode) { "Episode should not be null" },
            ).seasonEpisode.id
        }.toSet()
    }

    private suspend fun getAllEpisodes(
        show: Show,
        selectedEpisode: Episode,
    ): List<Episode> {
        return remoteShowsSource.getSeasons(show.ids.trakt)
            .filter { it.number > 0 && it.number <= selectedEpisode.season }
            .asyncMap {
                remoteEpisodesSource.getSeason(
                    showId = show.ids.trakt,
                    season = it.number,
                ).asyncMap { e ->
                    Episode.fromDto(e)
                }
            }
            .flatten()
    }
}
