package tv.trakt.trakt.app.core.shows.usecase

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.app.core.shows.ShowsConfig.SHOWS_SECTION_LIMIT
import tv.trakt.trakt.app.core.shows.data.remote.ShowsRemoteDataSource
import tv.trakt.trakt.common.core.shows.data.local.ShowLocalDataSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.nowLocalDay
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.fromDto
import java.time.Year

internal class GetPopularShowsUseCase(
    private val remoteSource: ShowsRemoteDataSource,
    private val localSource: ShowLocalDataSource,
) {
    suspend fun getPopularShows(
        limit: Int = SHOWS_SECTION_LIMIT,
        page: Int = 1,
    ): ImmutableList<Show> {
        return remoteSource.getPopularShows(
            limit = limit,
            page = page,
            years = getYearsRange(),
        )
            .asyncMap {
                Show.fromDto(it)
            }
            .toImmutableList()
            .also {
                localSource.upsertShows(it)
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
