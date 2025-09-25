package tv.trakt.trakt.core.home.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.common.helpers.extensions.popUpToTop
import tv.trakt.trakt.core.home.HomeScreen

@Serializable
internal data object HomeDestination

internal fun NavGraphBuilder.homeScreen(
    onNavigateToProfile: () -> Unit,
    onNavigateToShows: () -> Unit,
    onNavigateToMovies: () -> Unit,
    onNavigateToAllUpNext: () -> Unit,
    onNavigateToAllPersonal: () -> Unit,
    onNavigateToAllSocial: () -> Unit,
) {
    composable<HomeDestination> {
        HomeScreen(
            viewModel = koinViewModel(),
            onNavigateToProfile = onNavigateToProfile,
            onNavigateToShows = onNavigateToShows,
            onNavigateToMovies = onNavigateToMovies,
            onNavigateToAllUpNext = onNavigateToAllUpNext,
            onNavigateToAllPersonal = onNavigateToAllPersonal,
            onNavigateToAllSocial = onNavigateToAllSocial,
        )
    }
}

internal fun NavController.navigateToHome() {
    navigate(route = HomeDestination) {
        popUpToTop(this@navigateToHome)
    }
}
