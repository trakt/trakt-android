package tv.trakt.trakt.core.discover.sections.anticipated.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.data.local.AnticipatedShowsLocalSource
import tv.trakt.trakt.common.data.local.ShowLocalSource
import tv.trakt.trakt.common.data.remote.ShowsRemoteSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.discover.DiscoverItem
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.core.discover.DiscoverConfig.DEFAULT_SECTION_LIMIT
import tv.trakt.trakt.core.settings.usecases.SeasonalThemeUseCase
import java.time.temporal.ChronoUnit.DAYS

internal class GetAnticipatedShowsUseCase(
    private val remoteSource: ShowsRemoteSource,
    private val localAnticipatedSource: AnticipatedShowsLocalSource,
    private val localShowSource: ShowLocalSource,
    private val seasonalThemeUseCase: SeasonalThemeUseCase,
) {
    suspend fun getLocalShows(): ImmutableList<DiscoverItem.ShowItem> {
        return localAnticipatedSource.getShows()
            .sortedByDescending { it.count }
            .toImmutableList()
            .also {
                localShowSource.upsertShows(
                    it.asyncMap { item -> item.show },
                )
            }
    }

    suspend fun getShows(
        limit: Int = DEFAULT_SECTION_LIMIT,
        page: Int = 1,
        skipLocal: Boolean = false,
        filters: GlobalFilter,
    ): ImmutableList<DiscoverItem.ShowItem> {
        return remoteSource.getAnticipated(
            page = page,
            limit = limit,
            theme = seasonalThemeUseCase.activeSeason()?.remoteValue,
            endDate = nowUtcInstant().plus(365, DAYS).truncatedTo(DAYS),
            filters = filters,
        )
            .asyncMap {
                DiscoverItem.ShowItem(
                    show = Show.fromDto(it.show),
                    count = it.listCount,
                )
            }
            .toImmutableList()
            .also { shows ->
                if (!skipLocal) {
                    localAnticipatedSource.setShows(
                        shows = shows.take(DEFAULT_SECTION_LIMIT),
                    )
                }

                localShowSource.upsertShows(
                    shows.asyncMap { item -> item.show },
                )
            }
    }
}
