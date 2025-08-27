package tv.trakt.trakt.core.home.sections.upcoming.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.nowLocal
import tv.trakt.trakt.common.helpers.extensions.toInstant
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.model.toTraktId
import tv.trakt.trakt.core.episodes.model.Episode
import tv.trakt.trakt.core.episodes.model.fromDto
import tv.trakt.trakt.core.home.HomeConfig.HOME_UPCOMING_DAYS_LIMIT
import tv.trakt.trakt.core.home.sections.upcoming.data.local.HomeUpcomingLocalDataSource
import tv.trakt.trakt.core.home.sections.upcoming.model.HomeUpcomingItem
import tv.trakt.trakt.core.profile.data.remote.UserRemoteDataSource
import java.time.Instant

private val premiereValues = listOf("season_premiere", "series_premiere")
private val finaleValues = listOf("season_finale", "series_finale")

// TODO Add movies support.
internal class GetUpcomingUseCase(
    private val remoteUserSource: UserRemoteDataSource,
    private val localDataSource: HomeUpcomingLocalDataSource,
) {
    suspend fun getLocalUpcoming(): ImmutableList<HomeUpcomingItem> {
        return localDataSource.getItems()
            .sortedBy { it.releasedAt }
            .toImmutableList()
    }

    suspend fun getUpcoming(): ImmutableList<HomeUpcomingItem> {
        val remoteItems = remoteUserSource.getShowsCalendar(
            startDate = nowLocal().toLocalDate(),
            days = HOME_UPCOMING_DAYS_LIMIT,
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
                val releaseAt = it.firstAired.toInstant()
                if (releaseAt.isBefore(Instant.now())) {
                    return@asyncMap null
                }

                val isFullSeason = fullSeasonItems[it.show.ids.trakt] != null
                if (isFullSeason && it.episode.number > 1) {
                    return@asyncMap null
                }

                HomeUpcomingItem.EpisodeItem(
                    id = it.episode.ids.trakt.toTraktId(),
                    releasedAt = releaseAt,
                    episode = Episode.fromDto(it.episode),
                    show = Show.fromDto(it.show),
                    isFullSeason = isFullSeason,
                )
            }
            .filterNotNull()
            .sortedBy { it.releasedAt }
            .toImmutableList()
            .also {
                localDataSource.addItems(
                    items = it,
                )
            }
    }
}
