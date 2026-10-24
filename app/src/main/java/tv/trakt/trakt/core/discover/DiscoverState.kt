package tv.trakt.trakt.core.discover

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.core.user.UserCollectionState
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.core.calendar.model.CalendarView

@Immutable
internal data class DiscoverState(
    val collection: UserCollectionState = UserCollectionState.Default,
    val user: UserState = UserState(),
    val releasesView: CalendarView = CalendarView.Weekly,
) {
    data class UserState(
        val user: User? = null,
        val loading: LoadingState = LoadingState.Idle,
    ) {
        val isAuthenticated: Boolean
            get() = user != null
    }
}
