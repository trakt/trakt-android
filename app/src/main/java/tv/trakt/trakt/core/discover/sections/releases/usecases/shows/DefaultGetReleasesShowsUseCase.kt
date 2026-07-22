package tv.trakt.trakt.core.discover.sections.releases.usecases.shows

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.core.episodes.data.local.EpisodeLocalDataSource
import tv.trakt.trakt.common.core.shows.data.local.ShowLocalDataSource
import tv.trakt.trakt.common.helpers.extensions.toLocalDay
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.core.calendar.model.CalendarItem.EpisodeItem
import tv.trakt.trakt.core.discover.sections.releases.data.local.shows.ReleasesShowsLocalDataSource
import tv.trakt.trakt.core.discover.sections.releases.usecases.shows.ReleaseType.All
import tv.trakt.trakt.core.discover.sections.releases.usecases.shows.ReleaseType.Finale
import tv.trakt.trakt.core.discover.sections.releases.usecases.shows.ReleaseType.Premiere
import tv.trakt.trakt.core.shows.data.remote.ShowsRemoteDataSource
import java.time.Instant

internal class DefaultGetReleasesShowsUseCase(
    private val remoteSource: ShowsRemoteDataSource,
    private val localSource: ReleasesShowsLocalDataSource,
    private val localShowSource: ShowLocalDataSource,
    private val localEpisodeSource: EpisodeLocalDataSource,
) : GetReleasesShowsUseCase {
    override suspend fun clearLocal() {
        localSource.clear()
    }

    override suspend fun getLocalShows(): ImmutableList<EpisodeItem> {
        return localSource.getItems()
            .toImmutableList()
            .also {
                localShowSource.upsertShows(it.map { item -> item.show })
                localEpisodeSource.upsertEpisodes(it.flatMap { item -> item.episodes })
            }
    }

    override suspend fun getShows(
        startDate: Instant,
        days: Int,
        skipLocal: Boolean,
        filters: GlobalFilter,
        type: ReleaseType,
    ): ImmutableList<EpisodeItem> {
        val data = when (type) {
            All -> remoteSource.getReleases(
                startDate = startDate,
                days = days,
                filters = filters,
            ).map {
                Show.fromDto(it.show!!) to Episode.fromDto(it.episode!!)
            }
            Premiere -> remoteSource.getReleasesPremieres(
                startDate = startDate,
                days = days,
                filters = filters,
            ).map {
                Show.fromDto(it.show) to Episode.fromDto(it.episode)
            }
            Finale -> remoteSource.getReleasesFinales(
                startDate = startDate,
                days = days,
                filters = filters,
            ).map {
                Show.fromDto(it.show) to Episode.fromDto(it.episode)
            }
        }

        return data
            .filter { it.second.season > 0 }
            // Group a show's episodes that share the same release day into one item,
            // so a same-day batch renders as a single card with a combined list.
            .groupBy { (show, episode) -> show.ids.trakt to episode.releasedAt?.toLocalDay() }
            .map { (_, entries) ->
                val episodes = entries
                    .map { (_, episode) -> episode }
                    .sortedBy { it.number }
                    .toImmutableList()

                EpisodeItem(
                    watched = false,
                    episodes = episodes,
                    show = entries.first().first,
                    isFullSeason = episodes.isFullSeason(),
                )
            }
            .toImmutableList()
            .also { shows ->
                if (!skipLocal) {
                    localSource.setItems(items = shows)
                }

                localShowSource.upsertShows(shows.map { it.show })
                localEpisodeSource.upsertEpisodes(shows.flatMap { it.episodes })
            }
    }
}

// A same-day batch is a full season when it spans more than one episode and
// carries both the season premiere and the season finale.
private fun List<Episode>.isFullSeason(): Boolean {
    if (size <= 1) return false
    val hasPremiere = any { it.type?.isPremiere == true }
    val hasFinale = any { it.type?.isFinale == true }
    return hasPremiere && hasFinale
}
