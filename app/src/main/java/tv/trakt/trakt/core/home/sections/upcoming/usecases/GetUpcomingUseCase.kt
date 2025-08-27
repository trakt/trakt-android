package tv.trakt.trakt.core.home.sections.upcoming.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.core.episodes.model.Episode
import tv.trakt.trakt.core.episodes.model.fromDto
import tv.trakt.trakt.core.home.sections.upcoming.model.CalendarShow
import tv.trakt.trakt.core.profile.data.remote.UserRemoteDataSource
import tv.trakt.trakt.common.helpers.extensions.nowLocal
import tv.trakt.trakt.common.helpers.extensions.toZonedDateTime
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.fromDto
import java.time.ZonedDateTime

private val premiereValues = listOf("season_premiere", "series_premiere")
private val finaleValues = listOf("season_finale", "series_finale")

internal class GetUpcomingUseCase(
    private val remoteUserSource: UserRemoteDataSource,
) {
    suspend fun getCalendar(): ImmutableList<CalendarShow> {
        // For now, return empty list until we implement calendar API in app module
        // TODO: Add calendar functionality to UserRemoteDataSource
        return emptyList<CalendarShow>().toImmutableList()
    }
    
    // TODO: Implement this method when calendar API is available
    private suspend fun getCalendarImplementation(): ImmutableList<CalendarShow> {
        /*
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
            .mapNotNull {
                val releaseAt = it.firstAired.toZonedDateTime()
                if (releaseAt.isBefore(nowUtc())) {
                    return@mapNotNull null
                }

                val isFullSeason = fullSeasonItems[it.show.ids.trakt] != null
                if (isFullSeason && it.episode.number > 1) {
                    return@mapNotNull null
                }

                CalendarShow(
                    show = Show.fromDto(it.show),
                    episode = Episode.fromDto(it.episode),
                    releaseAt = releaseAt,
                    isFullSeason = isFullSeason,
                )
            }
            .toImmutableList()
        */
        return emptyList<CalendarShow>().toImmutableList()
    }
}
