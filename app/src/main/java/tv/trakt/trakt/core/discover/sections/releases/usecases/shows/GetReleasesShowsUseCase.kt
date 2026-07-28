package tv.trakt.trakt.core.discover.sections.releases.usecases.shows

import androidx.annotation.StringRes
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.core.calendar.model.CalendarItem
import tv.trakt.trakt.resources.R
import java.time.Instant

internal enum class ReleaseType(
    @param:StringRes val displayRes: Int,
) {
    All(displayRes = R.string.option_text_all),
    Premiere(displayRes = R.string.tag_text_premiere),
    Finale(displayRes = R.string.tag_text_finale),
}

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
