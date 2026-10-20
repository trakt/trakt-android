package tv.trakt.trakt.app.core.shows.usecase

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.app.core.shows.ShowsConfig.SHOWS_SECTION_LIMIT
import tv.trakt.trakt.common.data.local.ShowLocalSource
import tv.trakt.trakt.common.data.remote.ShowsRemoteSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.nowLocalDay
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import java.time.Year

internal class GetPopularShowsUseCase(
    private val remoteSource: ShowsRemoteSource,
    private val localSource: ShowLocalSource,
) {
    suspend fun getPopularShows(
        limit: Int = SHOWS_SECTION_LIMIT,
        page: Int = 1,
    ): ImmutableList<Show> {
        return remoteSource.getPopular(
            page = page,
            limit = limit,
            filters = GlobalFilter.Default.copy(years = getYearsRange().let { it to it }),
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
