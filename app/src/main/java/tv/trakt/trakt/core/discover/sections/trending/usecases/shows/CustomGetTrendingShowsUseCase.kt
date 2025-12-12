package tv.trakt.trakt.core.discover.sections.trending.usecases.shows

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.core.shows.data.local.ShowLocalDataSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.core.discover.DiscoverConfig.DEFAULT_SECTION_LIMIT
import tv.trakt.trakt.core.discover.model.DiscoverItem
import tv.trakt.trakt.core.discover.sections.trending.data.local.shows.TrendingShowsLocalDataSource
import tv.trakt.trakt.core.discover.sections.trending.usecases.GetTrendingShowsUseCase
import tv.trakt.trakt.core.main.usecases.CustomThemeUseCase
import tv.trakt.trakt.core.shows.data.remote.ShowsRemoteDataSource

internal class CustomGetTrendingShowsUseCase(
    private val remoteSource: ShowsRemoteDataSource,
    private val localTrendingSource: TrendingShowsLocalDataSource,
    private val localShowSource: ShowLocalDataSource,
    private val customThemeUseCase: CustomThemeUseCase,
) : GetTrendingShowsUseCase {
    override suspend fun getLocalShows(): ImmutableList<DiscoverItem.ShowItem> {
        return localTrendingSource.getShows()
            .sortedByDescending { it.count }
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
    ): ImmutableList<DiscoverItem.ShowItem> {
        val config = customThemeUseCase.getConfig()
        val filters = config.theme?.filters

        val customGenres = when {
            config.enabled -> filters?.shows?.trending?.genres
            else -> null
        }
        val customSubgenres = when {
            config.enabled -> filters?.shows?.trending?.subgenres
            else -> null
        }

        val customYears = when {
            config.enabled -> filters?.shows?.trending?.years?.toString()
            else -> null
        }

        return remoteSource.getTrending(
            page = page,
            limit = limit,
            genres = customGenres,
            subgenres = customSubgenres,
            years = customYears,
        )
            .asyncMap {
                DiscoverItem.ShowItem(
                    show = Show.fromDto(it.show),
                    count = it.watchers,
                )
            }
            .toImmutableList()
            .also { shows ->
                if (!skipLocal) {
                    localTrendingSource.addShows(
                        shows = shows.take(DEFAULT_SECTION_LIMIT),
                        addedAt = nowUtcInstant(),
                    )
                }

                localShowSource.upsertShows(
                    shows.asyncMap { item -> item.show },
                )
            }
    }
}
