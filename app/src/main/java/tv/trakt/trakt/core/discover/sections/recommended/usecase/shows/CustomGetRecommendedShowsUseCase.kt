package tv.trakt.trakt.core.discover.sections.recommended.usecase.shows

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.core.shows.data.local.ShowLocalDataSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.core.discover.DiscoverConfig.DEFAULT_SECTION_LIMIT
import tv.trakt.trakt.core.discover.model.DiscoverItem
import tv.trakt.trakt.core.discover.sections.recommended.data.local.shows.RecommendedShowsLocalDataSource
import tv.trakt.trakt.core.discover.sections.recommended.usecase.GetRecommendedShowsUseCase
import tv.trakt.trakt.core.main.usecases.CustomThemeUseCase
import tv.trakt.trakt.core.shows.data.remote.ShowsRemoteDataSource
import java.time.Instant

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
    ): ImmutableList<DiscoverItem.ShowItem> {
        val config = customThemeUseCase.getConfig()

        val customGenres = when {
            config.enabled -> config.theme?.filters?.shows?.recommended?.genres
            else -> null
        }
        val customSubgenres = when {
            config.enabled -> config.theme?.filters?.shows?.recommended?.subgenres
            else -> null
        }

        val customYears = when {
            config.enabled -> config.theme?.filters?.shows?.recommended?.years?.toString()
            else -> null
        }

        return remoteSource.getRecommended(
            limit = limit,
            years = customYears,
            genres = customGenres,
            subgenres = customSubgenres,
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
                    localRecommendedSource.addShows(
                        shows = shows.take(DEFAULT_SECTION_LIMIT),
                        addedAt = Instant.now(),
                    )
                }

                localShowSource.upsertShows(
                    shows.asyncMap { item -> item.show },
                )
            }
    }
}
