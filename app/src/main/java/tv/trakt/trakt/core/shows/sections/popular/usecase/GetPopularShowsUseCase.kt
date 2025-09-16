package tv.trakt.trakt.core.shows.sections.popular.usecase

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.nowLocalDay
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.core.shows.data.remote.ShowsRemoteDataSource
import tv.trakt.trakt.core.shows.sections.popular.data.local.PopularShowsLocalDataSource
import java.time.Instant
import java.time.Year

private const val DEFAULT_LIMIT = 24
internal const val DEFAULT_ALL_LIMIT = 102

internal class GetPopularShowsUseCase(
    private val remoteSource: ShowsRemoteDataSource,
    private val localPopularSource: PopularShowsLocalDataSource,
) {
    suspend fun getLocalShows(): ImmutableList<Show> {
        return localPopularSource.getShows()
            .toImmutableList()
    }

    suspend fun getShows(
        limit: Int = DEFAULT_LIMIT,
        page: Int = 1,
        skipLocal: Boolean = false,
    ): ImmutableList<Show> {
        return remoteSource.getPopular(
            page = page,
            limit = limit,
            years = getYearsRange(),
        )
            .asyncMap {
                Show.fromDto(it)
            }
            .toImmutableList()
            .also { shows ->
                if (skipLocal) return@also
                localPopularSource.addShows(
                    shows = shows.take(DEFAULT_LIMIT),
                    addedAt = Instant.now(),
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
