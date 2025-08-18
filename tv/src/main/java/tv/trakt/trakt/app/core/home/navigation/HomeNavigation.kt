package tv.trakt.trakt.app.core.home.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.app.core.episodes.model.Episode
import tv.trakt.trakt.app.core.home.HomeScreen
import tv.trakt.trakt.common.helpers.extensions.popUpToTop
import tv.trakt.trakt.common.model.TraktId

@Serializable
internal data object HomeDestination

internal fun NavGraphBuilder.homeScreen(
    onNavigateToAuth: () -> Unit,
    onNavigateToMovie: (TraktId) -> Unit,
    onNavigateToEpisode: (showId: TraktId, episode: Episode) -> Unit,
    onNavigateToViewAll: () -> Unit,
) {
    composable<HomeDestination> {
        HomeScreen(
            viewModel = koinViewModel(),
            onNavigateToAuth = onNavigateToAuth,
            onNavigateToMovie = onNavigateToMovie,
            onNavigateToEpisode = onNavigateToEpisode,
            onNavigateToViewAll = onNavigateToViewAll,
        )
    }
}

internal fun NavController.navigateToHome() {
    navigate(route = HomeDestination) {
        popUpToTop(this@navigateToHome)
    }
}
