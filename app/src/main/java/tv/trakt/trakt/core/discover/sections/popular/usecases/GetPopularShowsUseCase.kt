package tv.trakt.trakt.core.discover.sections.popular.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.data.local.PopularShowsLocalSource
import tv.trakt.trakt.common.data.local.ShowLocalSource
import tv.trakt.trakt.common.data.remote.ShowsRemoteSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.discover.DiscoverItem
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.core.discover.DiscoverConfig
import tv.trakt.trakt.core.settings.usecases.SeasonalThemeUseCase

internal class GetPopularShowsUseCase(
    private val remoteSource: ShowsRemoteSource,
    private val localPopularSource: PopularShowsLocalSource,
    private val localShowSource: ShowLocalSource,
    private val seasonalThemeUseCase: SeasonalThemeUseCase,
) {
    suspend fun getLocalShows(): ImmutableList<DiscoverItem.ShowItem> {
        return localPopularSource.getShows()
            .toImmutableList()
            .also {
                localShowSource.upsertShows(
                    it.asyncMap { item -> item.show },
                )
            }
    }

    suspend fun getShows(
        limit: Int = DiscoverConfig.DEFAULT_SECTION_LIMIT,
        page: Int = 1,
        skipLocal: Boolean = false,
        filters: GlobalFilter,
    ): ImmutableList<DiscoverItem.ShowItem> {
        return remoteSource.getPopular(
            page = page,
            limit = limit,
            theme = seasonalThemeUseCase.activeSeason()?.remoteValue,
            filters = filters,
        )
            .mapIndexed { index, showDto ->
                DiscoverItem.ShowItem(
                    show = Show.fromDto(showDto),
                    count = index + 1,
                )
            }
            .toImmutableList()
            .also { shows ->
                if (!skipLocal) {
                    localPopularSource.setShows(
                        shows = shows.take(DiscoverConfig.DEFAULT_SECTION_LIMIT),
                    )
                }

                localShowSource.upsertShows(
                    shows.asyncMap { item -> item.show },
                )
            }
    }
}
