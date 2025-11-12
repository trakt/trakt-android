package tv.trakt.trakt.core.home.sections.watchlist.features.all.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.home.sections.watchlist.features.all.AllHomeWatchlistScreen

@Serializable
internal data object HomeWatchlistDestination

internal fun NavGraphBuilder.homeWatchlistScreen(
    onNavigateToShow: (TraktId) -> Unit,
    onNavigateToMovie: (TraktId) -> Unit,
    onNavigateBack: () -> Unit,
) {
    composable<HomeWatchlistDestination> {
        AllHomeWatchlistScreen(
            viewModel = koinViewModel(),
            onShowClick = onNavigateToShow,
            onMovieClick = onNavigateToMovie,
            onNavigateBack = onNavigateBack,
        )
    }
}

internal fun NavController.navigateToAllHomeWatchlist() {
    navigate(route = HomeWatchlistDestination)
}
