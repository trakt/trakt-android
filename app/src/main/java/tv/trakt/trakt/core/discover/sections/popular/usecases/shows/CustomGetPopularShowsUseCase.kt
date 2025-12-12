package tv.trakt.trakt.core.discover.sections.popular.usecases.shows

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.core.shows.data.local.ShowLocalDataSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.core.discover.DiscoverConfig.DEFAULT_SECTION_LIMIT
import tv.trakt.trakt.core.discover.model.DiscoverItem
import tv.trakt.trakt.core.discover.sections.popular.data.local.shows.PopularShowsLocalDataSource
import tv.trakt.trakt.core.discover.sections.popular.usecases.GetPopularShowsUseCase
import tv.trakt.trakt.core.main.usecases.CustomThemeUseCase
import tv.trakt.trakt.core.shows.data.remote.ShowsRemoteDataSource
import java.time.Instant

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
    ): ImmutableList<DiscoverItem.ShowItem> {
        val config = customThemeUseCase.getConfig()
        val filters = config.theme?.filters

        val customGenres = when {
            config.enabled -> filters?.shows?.popular?.genres
            else -> null
        }
        val customSubgenres = when {
            config.enabled -> filters?.shows?.popular?.subgenres
            else -> null
        }

        val customYears = when {
            config.enabled -> filters?.shows?.popular?.years?.toString()
            else -> null
        }

        return remoteSource.getPopular(
            page = page,
            limit = limit,
            genres = customGenres,
            subgenres = customSubgenres,
            years = customYears,
        ).asyncMap {
            DiscoverItem.ShowItem(
                show = Show.fromDto(it),
                count = 0,
            )
        }
            .toImmutableList()
            .also { shows ->
                if (!skipLocal) {
                    localPopularSource.addShows(
                        shows = shows.take(DEFAULT_SECTION_LIMIT),
                        addedAt = Instant.now(),
                    )
                }

                localShowSource.upsertShows(shows.asyncMap { it.show })
            }
    }
}
