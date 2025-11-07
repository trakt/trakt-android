package tv.trakt.trakt.core.discover.sections.popular.usecases.shows

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.core.shows.data.local.ShowLocalDataSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.nowLocalDay
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.core.discover.DiscoverConfig
import tv.trakt.trakt.core.discover.data.remote.ShowsRemoteDataSource
import tv.trakt.trakt.core.discover.model.DiscoverItem
import tv.trakt.trakt.core.discover.sections.popular.data.local.shows.PopularShowsLocalDataSource
import tv.trakt.trakt.core.discover.sections.popular.usecases.GetPopularShowsUseCase
import java.time.Instant
import java.time.Year

internal class DefaultGetPopularShowsUseCase(
    private val remoteSource: ShowsRemoteDataSource,
    private val localPopularSource: PopularShowsLocalDataSource,
    private val localShowSource: ShowLocalDataSource,
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
        return remoteSource.getPopular(
            page = page,
            limit = limit,
            years = getYearsRange().toString(),
        )
            .mapIndexed { index, showDto ->
                DiscoverItem.ShowItem(
                    show = Show.fromDto(showDto),
                    count = index + 1, // Use ranking position as count
                )
            }
            .toImmutableList()
            .also { shows ->
                if (!skipLocal) {
                    localPopularSource.addShows(
                        shows = shows.take(DiscoverConfig.DEFAULT_SECTION_LIMIT),
                        addedAt = Instant.now(),
                    )
                }

                localShowSource.upsertShows(
                    shows.asyncMap { item -> item.show },
                )
            }
    }

    private fun getYearsRange(): Int {
        val currentYear = Year.now().value
        val currentMonth = nowLocalDay().monthValue
        return if (currentMonth <= 3) {
            currentYear - 1
        } else {
            currentYear
        }
    }
}
