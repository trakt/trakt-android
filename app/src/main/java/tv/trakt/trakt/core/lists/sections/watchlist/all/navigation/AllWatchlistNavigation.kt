package tv.trakt.trakt.core.lists.sections.watchlist.all.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel
import tv.trakt.trakt.core.lists.sections.watchlist.all.AllWatchlistScreen

@Serializable
internal data object ListsWatchlistDestination

internal fun NavGraphBuilder.allWatchlistScreen(onNavigateBack: () -> Unit) {
    composable<ListsWatchlistDestination> {
        AllWatchlistScreen(
            viewModel = koinViewModel(),
            onNavigateBack = onNavigateBack,
        )
    }
}

internal fun NavController.navigateToWatchlist() {
    navigate(route = ListsWatchlistDestination)
}
