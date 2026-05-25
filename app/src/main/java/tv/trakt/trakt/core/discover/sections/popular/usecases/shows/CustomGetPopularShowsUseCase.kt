package tv.trakt.trakt.core.discover.sections.popular.usecases.shows

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.core.shows.data.local.ShowLocalDataSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.core.discover.DiscoverConfig.DEFAULT_SECTION_LIMIT
import tv.trakt.trakt.core.discover.model.DiscoverItem
import tv.trakt.trakt.core.discover.sections.popular.data.local.shows.PopularShowsLocalDataSource
import tv.trakt.trakt.core.discover.sections.popular.usecases.GetPopularShowsUseCase
import tv.trakt.trakt.core.main.usecases.CustomThemeUseCase
import tv.trakt.trakt.core.shows.data.remote.ShowsRemoteDataSource

internal class CustomGetPopularShowsUseCase(
    private val remoteSource: ShowsRemoteDataSource,
    private val localPopularSource: PopularShowsLocalDataSource,
    private val localShowSource: ShowLocalDataSource,
    private val customThemeUseCase: CustomThemeUseCase,
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
            filters = filters,
        ).asyncMap {
            DiscoverItem.ShowItem(
                show = Show.fromDto(it),
                count = 0,
            )
        }
            .toImmutableList()
            .also { shows ->
                if (!skipLocal) {
                    localPopularSource.setShows(
                        shows = shows.take(DEFAULT_SECTION_LIMIT),
                    )
                }

                localShowSource.upsertShows(shows.asyncMap { it.show })
            }
    }
}
