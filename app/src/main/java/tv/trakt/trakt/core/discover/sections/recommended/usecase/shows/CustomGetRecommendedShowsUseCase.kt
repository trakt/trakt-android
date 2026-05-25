package tv.trakt.trakt.core.discover.sections.recommended.usecase.shows

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.core.shows.data.local.ShowLocalDataSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.core.discover.DiscoverConfig.DEFAULT_SECTION_LIMIT
import tv.trakt.trakt.core.discover.model.DiscoverItem
import tv.trakt.trakt.core.discover.sections.recommended.data.local.shows.RecommendedShowsLocalDataSource
import tv.trakt.trakt.core.discover.sections.recommended.usecase.GetRecommendedShowsUseCase
import tv.trakt.trakt.core.main.usecases.CustomThemeUseCase
import tv.trakt.trakt.core.shows.data.remote.ShowsRemoteDataSource

internal class CustomGetRecommendedShowsUseCase(
    private val remoteSource: ShowsRemoteDataSource,
    private val localRecommendedSource: RecommendedShowsLocalDataSource,
    private val localShowSource: ShowLocalDataSource,
    private val customThemeUseCase: CustomThemeUseCase,
) : GetRecommendedShowsUseCase {
    override suspend fun getLocalShows(): ImmutableList<DiscoverItem.ShowItem> {
        return localRecommendedSource.getShows()
            .toImmutableList()
            .also { items ->
                val shows = items.asyncMap { it.show }
                localShowSource.upsertShows(shows)
            }
    }

    override suspend fun getShows(
        limit: Int,
        skipLocal: Boolean,
        filters: GlobalFilter,
    ): ImmutableList<DiscoverItem.ShowItem> {
        return remoteSource.getRecommended(
            limit = limit,
            filters = filters,
        )
            .asyncMap {
                DiscoverItem.ShowItem(
                    show = Show.fromDto(it),
                    count = 0, // No ranking for recommended shows
                )
            }
            .toImmutableList()
            .also { shows ->
                if (!skipLocal) {
                    localRecommendedSource.setShows(
                        shows = shows.take(DEFAULT_SECTION_LIMIT),
                    )
                }

                localShowSource.upsertShows(
                    shows.asyncMap { item -> item.show },
                )
            }
    }
}
