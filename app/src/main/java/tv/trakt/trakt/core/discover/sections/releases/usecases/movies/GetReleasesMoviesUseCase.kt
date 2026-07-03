package tv.trakt.trakt.core.discover.sections.releases.usecases.movies

import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.core.calendar.model.CalendarItem
import java.time.Instant

internal interface GetReleasesMoviesUseCase {
    suspend fun getLocalMovies(): ImmutableList<CalendarItem>

    suspend fun getMovies(
        startDate: Instant,
        days: Int,
        skipLocal: Boolean = false,
        filters: GlobalFilter,
    ): ImmutableList<CalendarItem>
}
