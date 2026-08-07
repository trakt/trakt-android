package tv.trakt.trakt.core.calendar.feature.monthly

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.ImmutableSet
import tv.trakt.trakt.common.helpers.DynamicStringResource
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.core.calendar.model.CalendarDayDisplay
import tv.trakt.trakt.core.calendar.model.CalendarItem
import tv.trakt.trakt.core.discover.sections.releases.model.ReleaseType
import java.time.LocalDate
import java.time.YearMonth

@Immutable
internal data class CalendarMonthlyState(
    val selectedMonth: YearMonth? = null,
    val filter: GlobalFilter? = null,
    val type: ReleaseType = ReleaseType.All,
    val display: CalendarDayDisplay = CalendarDayDisplay.Posters,
    val items: ImmutableMap<LocalDate, ImmutableList<CalendarItem>>? = null,
    val itemsLoading: ImmutableSet<TraktId>? = null,
    val navigateShow: TraktId? = null,
    val navigateMovie: TraktId? = null,
    val navigateEpisode: Pair<TraktId, Episode>? = null,
    val loading: LoadingState = LoadingState.Idle,
    val info: DynamicStringResource? = null,
    val error: Exception? = null,
)
