package tv.trakt.trakt.core.discover.sections.releases.usecases.shows

import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.core.calendar.model.CalendarItem
import tv.trakt.trakt.core.discover.sections.releases.model.ReleaseType
import java.time.Instant

internal interface GetReleasesShowsUseCase {
    suspend fun clearLocal()

    suspend fun getLocalShows(): ImmutableList<CalendarItem>

    suspend fun getShows(
        startDate: Instant,
        days: Int,
        skipLocal: Boolean = false,
        filters: GlobalFilter,
        type: ReleaseType = ReleaseType.All,
    ): ImmutableList<CalendarItem>
}
