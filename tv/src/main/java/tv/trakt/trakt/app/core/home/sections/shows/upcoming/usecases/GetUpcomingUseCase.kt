package tv.trakt.trakt.app.core.home.sections.shows.upcoming.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.app.core.episodes.data.local.EpisodeLocalDataSource
import tv.trakt.trakt.app.core.episodes.model.Episode
import tv.trakt.trakt.app.core.episodes.model.fromDto
import tv.trakt.trakt.app.core.home.sections.shows.upcoming.model.CalendarShow
import tv.trakt.trakt.app.core.profile.data.remote.ProfileRemoteDataSource
import tv.trakt.trakt.app.core.shows.data.local.ShowLocalDataSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.nowLocal
import tv.trakt.trakt.common.helpers.extensions.nowUtc
import tv.trakt.trakt.common.helpers.extensions.toZonedDateTime
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.fromDto

private val premiereValues = listOf("season_premiere", "series_premiere")
private val finaleValues = listOf("season_finale", "series_finale")

internal class GetUpcomingUseCase(
    private val remoteUserSource: ProfileRemoteDataSource,
    private val localShowSource: ShowLocalDataSource,
    private val localEpisodeSource: EpisodeLocalDataSource,
) {
    suspend fun getCalendar(): ImmutableList<CalendarShow> {
        val remoteItems = remoteUserSource.getUserShowsCalendar(
            startDate = nowLocal().toLocalDate(),
            days = 14,
        )

        val fullSeasonItems = remoteItems
            .groupBy { it.show.ids.trakt }
            .filter { (_, episodes) ->
                val isSeasonPremiere = episodes.any {
                    it.episode.episodeType?.value in premiereValues
                }

                val isSeasonFinale = episodes.any {
                    it.episode.episodeType?.value in finaleValues
                }

                return@filter episodes.size > 1 && isSeasonPremiere && isSeasonFinale
            }

        return remoteItems
            .asyncMap {
                val releaseAt = it.firstAired.toZonedDateTime()
                if (releaseAt.isBefore(nowUtc())) {
                    return@asyncMap null
                }

                val isFullSeason = fullSeasonItems[it.show.ids.trakt] != null
                if (isFullSeason && it.episode.number > 1) {
                    return@asyncMap null
                }

                CalendarShow(
                    show = Show.fromDto(it.show),
                    episode = Episode.fromDto(it.episode),
                    releaseAt = releaseAt,
                    isFullSeason = isFullSeason,
                )
            }
            .filterNotNull()
            .toImmutableList()
            .also {
                val shows = it.asyncMap { item -> item.show }
                val episodes = it.asyncMap { item -> item.episode }

                localShowSource.upsertShows(shows)
                localEpisodeSource.upsertEpisodes(episodes)
            }
    }
}
