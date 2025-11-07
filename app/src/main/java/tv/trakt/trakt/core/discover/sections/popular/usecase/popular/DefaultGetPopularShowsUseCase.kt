package tv.trakt.trakt.core.discover.sections.popular.usecase.popular

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.core.shows.data.local.ShowLocalDataSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.nowLocalDay
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.core.discover.data.remote.ShowsRemoteDataSource
import tv.trakt.trakt.core.discover.sections.popular.data.local.PopularShowsLocalDataSource
import tv.trakt.trakt.core.discover.sections.popular.usecase.DEFAULT_LIMIT
import tv.trakt.trakt.core.discover.sections.popular.usecase.GetPopularShowsUseCase
import java.time.Instant
import java.time.Year

internal class DefaultGetPopularShowsUseCase(
    private val remoteSource: ShowsRemoteDataSource,
    private val localPopularSource: PopularShowsLocalDataSource,
    private val localShowSource: ShowLocalDataSource,
) : GetPopularShowsUseCase {
    override suspend fun getLocalShows(): ImmutableList<Show> {
        return localPopularSource.getShows()
            .toImmutableList()
            .also {
                localShowSource.upsertShows(it)
            }
    }

    override suspend fun getShows(
        limit: Int,
        page: Int,
        skipLocal: Boolean,
    ): ImmutableList<Show> {
        return remoteSource.getPopular(
            page = page,
            limit = limit,
            years = getYearsRange().toString(),
        )
            .asyncMap {
                Show.fromDto(it)
            }
            .toImmutableList()
            .also { shows ->
                if (!skipLocal) {
                    localPopularSource.addShows(
                        shows = shows.take(DEFAULT_LIMIT),
                        addedAt = Instant.now(),
                    )
                }

                localShowSource.upsertShows(shows)
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
