package tv.trakt.trakt.core.today.stories.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel
import tv.trakt.trakt.core.today.stories.TodayStoriesScreen

@Serializable
internal data class TodayStoriesDestination(
    val startKey: String,
)

internal fun NavGraphBuilder.todayStoriesScreen(onNavigateBack: () -> Unit) {
    composable<TodayStoriesDestination> {
        TodayStoriesScreen(
            viewModel = koinViewModel(),
            onNavigateBack = onNavigateBack,
        )
    }
}

internal fun NavController.navigateToTodayStories(startKey: String) {
    navigate(route = TodayStoriesDestination(startKey = startKey))
}
