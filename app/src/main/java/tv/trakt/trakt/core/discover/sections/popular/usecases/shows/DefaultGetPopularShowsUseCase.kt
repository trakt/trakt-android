package tv.trakt.trakt.core.discover.sections.popular.usecases.shows

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.data.local.ShowLocalSource
import tv.trakt.trakt.common.data.remote.ShowsRemoteSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.core.discover.DiscoverConfig
import tv.trakt.trakt.core.discover.model.DiscoverItem
import tv.trakt.trakt.core.discover.sections.popular.data.local.shows.PopularShowsLocalSource
import tv.trakt.trakt.core.discover.sections.popular.usecases.GetPopularShowsUseCase
import tv.trakt.trakt.core.settings.usecases.SeasonalThemeUseCase

internal class DefaultGetPopularShowsUseCase(
    private val remoteSource: ShowsRemoteSource,
    private val localPopularSource: PopularShowsLocalSource,
    private val localShowSource: ShowLocalSource,
    private val seasonalThemeUseCase: SeasonalThemeUseCase,
) : GetPopularShowsUseCase {
    override suspend fun getLocalShows(): ImmutableList<DiscoverItem.ShowItem> {
        return localPopularSource.getShows()
            .toImmutableList()
            .also {
                localShowSource.upsertShows(
                    it.asyncMap { item -> item.show },
                )
            }
    }

    override suspend fun getShows(
        limit: Int,
        page: Int,
        skipLocal: Boolean,
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
