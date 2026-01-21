package tv.trakt.trakt.core.calendar.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.core.calendar.CalendarScreen

@Serializable
internal data object CalendarDestination

internal fun NavGraphBuilder.calendarScreen(
    onNavigateBack: () -> Unit,
) {
    composable<CalendarDestination> {
        CalendarScreen(
            viewModel = koinViewModel(),
            onNavigateBack = onNavigateBack,
        )
    }
}

internal fun NavController.navigateToCalendar() {
    navigate(route = CalendarDestination)
}
