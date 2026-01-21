package tv.trakt.trakt.core.calendar

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.User

@Immutable
internal data class CalendarState(
    val user: User? = null,
    val loading: LoadingState = LoadingState.IDLE,
    val error: Exception? = null,
)
