package tv.trakt.trakt.core.home

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.core.user.UserCollectionState
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.MediaMode
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.WhatsNew
import tv.trakt.trakt.core.calendar.model.CalendarView

@Immutable
internal data class HomeState(
    val mode: MediaMode? = null,
    val user: UserState = UserState(),
    val welcomeBanner: Boolean = false,
    val collection: UserCollectionState = UserCollectionState.Default,
    val whatsNew: WhatsNew? = null,
    val calendarView: CalendarView = CalendarView.Weekly,
) {
    data class UserState(
        val user: User? = null,
        val loading: LoadingState = LoadingState.Idle,
    ) {
        val isAuthenticated: Boolean
            get() = user != null
    }
}
