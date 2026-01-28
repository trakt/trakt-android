package tv.trakt.trakt.core.calendar

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.ImmutableSet
import tv.trakt.trakt.common.helpers.DynamicStringResource
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.core.calendar.model.CalendarItem
import java.time.LocalDate

@Immutable
internal data class CalendarState(
    val selectedStartDay: LocalDate,
    val items: ImmutableMap<LocalDate, ImmutableList<CalendarItem>>? = null,
    val itemsLoading: ImmutableSet<TraktId>? = null,
    val user: User? = null,
    val navigateShow: TraktId? = null,
    val navigateMovie: TraktId? = null,
    val navigateEpisode: Pair<TraktId, Episode>? = null,
    val loading: LoadingState = LoadingState.IDLE,
    val info: DynamicStringResource? = null,
    val error: Exception? = null,
)
