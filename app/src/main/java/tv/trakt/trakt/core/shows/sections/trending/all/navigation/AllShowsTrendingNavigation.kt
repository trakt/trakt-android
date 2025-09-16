package tv.trakt.trakt.core.shows.sections.trending.all.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.core.shows.sections.trending.all.AllShowsTrendingScreen

@Serializable
internal data object ShowsTrendingDestination

internal fun NavGraphBuilder.showsTrendingScreen(onNavigateBack: () -> Unit) {
    composable<ShowsTrendingDestination> {
        AllShowsTrendingScreen(
            viewModel = koinViewModel(),
            onNavigateBack = onNavigateBack,
        )
    }
}

internal fun NavController.navigateToTrendingShows() {
    navigate(route = ShowsTrendingDestination)
}
