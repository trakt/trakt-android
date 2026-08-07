package tv.trakt.trakt.core.calendar.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.calendar.feature.monthly.CalendarMonthlyScreen
import tv.trakt.trakt.core.calendar.feature.weekly.CalendarScreen
import tv.trakt.trakt.core.calendar.model.CalendarView

@Serializable
internal data object CalendarDestination

@Serializable
internal data object CalendarMonthlyDestination

internal fun NavGraphBuilder.calendarScreen(
    onNavigateBack: () -> Unit,
    onEpisodeClick: (showId: TraktId, episode: Episode) -> Unit,
    onShowClick: (TraktId) -> Unit,
    onMovieClick: (TraktId) -> Unit,
    onViewClick: (CalendarView) -> Unit,
) {
    composable<CalendarDestination> {
        CalendarScreen(
            viewModel = koinViewModel(),
            onEpisodeClick = onEpisodeClick,
            onShowClick = onShowClick,
            onMovieClick = onMovieClick,
            onViewClick = onViewClick,
            onNavigateBack = onNavigateBack,
        )
    }
}

internal fun NavGraphBuilder.calendarMonthlyScreen(
    onNavigateBack: () -> Unit,
    onEpisodeClick: (showId: TraktId, episode: Episode) -> Unit,
    onShowClick: (TraktId) -> Unit,
    onMovieClick: (TraktId) -> Unit,
    onViewClick: (CalendarView) -> Unit,
) {
    composable<CalendarMonthlyDestination> {
        CalendarMonthlyScreen(
            viewModel = koinViewModel(),
            onEpisodeClick = onEpisodeClick,
            onShowClick = onShowClick,
            onMovieClick = onMovieClick,
            onViewClick = onViewClick,
            onNavigateBack = onNavigateBack,
        )
    }
}

internal fun NavController.navigateToCalendar(view: CalendarView) {
    when (view) {
        CalendarView.Weekly -> navigate(route = CalendarDestination)
        CalendarView.Monthly -> navigate(route = CalendarMonthlyDestination)
    }
}

// Swaps the calendar layout in place, so switching views doesn't stack up on the
// back stack. Popping a destination that isn't on the stack is a no-op.
internal fun NavController.switchCalendarView(view: CalendarView) {
    when (view) {
        CalendarView.Weekly -> navigate(route = CalendarDestination) {
            popUpTo(CalendarMonthlyDestination) { inclusive = true }
        }

        CalendarView.Monthly -> navigate(route = CalendarMonthlyDestination) {
            popUpTo(CalendarDestination) { inclusive = true }
        }
    }
}
