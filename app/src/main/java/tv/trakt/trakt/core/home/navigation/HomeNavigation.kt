package tv.trakt.trakt.core.home.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import tv.trakt.trakt.common.helpers.extensions.popUpToTop
import tv.trakt.trakt.core.home.HomeScreen
import tv.trakt.trakt.tv.common.model.TraktId

@Serializable
internal data object HomeDestination

internal fun NavGraphBuilder.homeScreen(onNavigateToHome: (TraktId) -> Unit) {
    composable<HomeDestination> {
        HomeScreen(
            onNavigateToHome = onNavigateToHome,
        )
    }
}

internal fun NavController.navigateToHome() {
    navigate(route = HomeDestination) {
        popUpToTop(this@navigateToHome)
    }
}
