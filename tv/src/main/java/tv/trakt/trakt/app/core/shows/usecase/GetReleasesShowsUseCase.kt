package tv.trakt.trakt.app.core.shows.usecase

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.app.core.home.sections.shows.upcoming.model.HomeUpcomingItem
import tv.trakt.trakt.app.core.shows.ShowsConfig.SHOWS_SECTION_LIMIT
import tv.trakt.trakt.app.core.shows.data.remote.ShowsRemoteDataSource
import tv.trakt.trakt.common.core.episodes.data.local.EpisodeLocalDataSource
import tv.trakt.trakt.common.core.shows.data.local.ShowLocalDataSource
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.helpers.extensions.toLocalDay
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.fromDto
import java.time.LocalDate
import java.time.temporal.ChronoUnit.DAYS

private const val DAYS_OFFSET = 1L
private const val DAYS_RANGE = 14

internal class GetReleasesShowsUseCase(
    private val remoteSource: ShowsRemoteDataSource,
    private val localShowSource: ShowLocalDataSource,
    private val localEpisodeSource: EpisodeLocalDataSource,
) {
    suspend fun getReleases(
        limit: Int = SHOWS_SECTION_LIMIT,
        range: Int = DAYS_RANGE,
    ): ImmutableList<HomeUpcomingItem.EpisodeItem> {
        val startDate = nowUtcInstant()
        val startDay = startDate.toLocalDay()

        val remoteItems = remoteSource.getReleases(
            startDate = startDate.minus(DAYS_OFFSET, DAYS),
            days = range,
        )

        return remoteItems
            .mapNotNull { dto ->
                val show = dto.show ?: return@mapNotNull null
                val episode = dto.episode ?: return@mapNotNull null
                Show.fromDto(show) to Episode.fromDto(episode)
            }
            .filter { (_, episode) -> episode.season > 0 }
            // Group a show's episodes that share the same release day into one item,
            // so a same-day batch renders as a single card with a combined list.
            .groupBy { (show, episode) -> show.ids.trakt to episode.releasedAt?.toLocalDay() }
            .map { (_, entries) ->
                val episodes = entries
                    .map { (_, episode) -> episode }
                    .sortedBy { it.number }
                    .toImmutableList()

                HomeUpcomingItem.EpisodeItem(
                    show = entries.first().first,
                    episodes = episodes,
                    isFullSeason = episodes.isFullSeason(),
                )
            }
            .filter { (it.episode.releasedAt?.toLocalDay() ?: LocalDate.MIN) >= startDay }
            .sortedBy { it.releaseAt }
            .take(limit)
            .toImmutableList()
            .also { items ->
                localShowSource.upsertShows(items.map { it.show })
                localEpisodeSource.upsertEpisodes(items.flatMap { it.episodes })
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
