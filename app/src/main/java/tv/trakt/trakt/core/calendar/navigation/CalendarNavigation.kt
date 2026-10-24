package tv.trakt.trakt.core.calendar.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.calendar.feature.monthly.CalendarMonthlyScreen
import tv.trakt.trakt.core.calendar.feature.weekly.CalendarScreen
import tv.trakt.trakt.core.calendar.model.CalendarSource
import tv.trakt.trakt.core.calendar.model.CalendarView

@Serializable
internal data class CalendarDestination(
    val source: CalendarSource,
)

@Serializable
internal data class CalendarMonthlyDestination(
    val source: CalendarSource,
)

internal fun NavGraphBuilder.calendarScreen(
    onNavigateBack: () -> Unit,
    onEpisodeClick: (showId: TraktId, episode: Episode) -> Unit,
    onShowClick: (TraktId) -> Unit,
    onMovieClick: (TraktId) -> Unit,
    onViewClick: (CalendarView, CalendarSource) -> Unit,
) {
    composable<CalendarDestination> { entry ->
        val source = entry.toRoute<CalendarDestination>().source
        CalendarScreen(
            viewModel = koinViewModel(),
            onEpisodeClick = onEpisodeClick,
            onShowClick = onShowClick,
            onMovieClick = onMovieClick,
            onViewClick = { onViewClick(it, source) },
            onNavigateBack = onNavigateBack,
        )
    }
}

internal fun NavGraphBuilder.calendarMonthlyScreen(
    onNavigateBack: () -> Unit,
    onEpisodeClick: (showId: TraktId, episode: Episode) -> Unit,
    onShowClick: (TraktId) -> Unit,
    onMovieClick: (TraktId) -> Unit,
    onViewClick: (CalendarView, CalendarSource) -> Unit,
) {
    composable<CalendarMonthlyDestination> { entry ->
        val source = entry.toRoute<CalendarMonthlyDestination>().source
        CalendarMonthlyScreen(
            viewModel = koinViewModel(),
            onEpisodeClick = onEpisodeClick,
            onShowClick = onShowClick,
            onMovieClick = onMovieClick,
            onViewClick = { onViewClick(it, source) },
            onNavigateBack = onNavigateBack,
        )
    }
}

internal fun NavController.navigateToCalendar(
    view: CalendarView,
    source: CalendarSource,
) {
    when (view) {
        CalendarView.Weekly -> navigate(route = CalendarDestination(source))
        CalendarView.Monthly -> navigate(route = CalendarMonthlyDestination(source))
    }
}

internal fun NavController.switchCalendarView(
    view: CalendarView,
    source: CalendarSource,
) {
    when (view) {
        CalendarView.Weekly -> navigate(route = CalendarDestination(source)) {
            popUpTo<CalendarMonthlyDestination> { inclusive = true }
        }

        CalendarView.Monthly -> navigate(route = CalendarMonthlyDestination(source)) {
            popUpTo<CalendarDestination> { inclusive = true }
        }
    }
}
