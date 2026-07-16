package tv.trakt.trakt.app.core.home.sections.startwatching.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.app.core.home.sections.startwatching.viewall.WatchlistViewAllScreen
import tv.trakt.trakt.common.model.TraktId

@Serializable
internal data object HomeWatchlistDestination

internal fun NavGraphBuilder.homeWatchlistScreen(
    onNavigateToMovie: (TraktId) -> Unit,
    onNavigateToShow: (TraktId) -> Unit,
) {
    composable<HomeWatchlistDestination> {
        WatchlistViewAllScreen(
            viewModel = koinViewModel(),
            onNavigateToMovie = onNavigateToMovie,
            onNavigateToShow = onNavigateToShow,
        )
    }
}

internal fun NavController.navigateToHomeWatchlist() {
    navigate(route = HomeWatchlistDestination)
}
