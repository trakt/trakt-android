package tv.trakt.trakt.core.shows.sections.recommended.all.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.core.shows.sections.recommended.all.AllShowsRecommendedScreen

@Serializable
internal data object ShowsRecommendedDestination

internal fun NavGraphBuilder.showsRecommendedScreen(onNavigateBack: () -> Unit) {
    composable<ShowsRecommendedDestination> {
        AllShowsRecommendedScreen(
            viewModel = koinViewModel(),
            onNavigateBack = onNavigateBack,
        )
    }
}

internal fun NavController.navigateToRecommendedShows() {
    navigate(route = ShowsRecommendedDestination)
}
