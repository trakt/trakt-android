package tv.trakt.trakt.core.lists.features.reorder.watchlist.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.core.lists.features.reorder.watchlist.WatchlistReorderScreen

@Serializable
internal data object WatchlistReorderDestination

internal fun NavGraphBuilder.watchlistReorderScreen(onNavigateBack: () -> Unit) {
    composable<WatchlistReorderDestination> {
        WatchlistReorderScreen(
            viewModel = koinViewModel(),
            onNavigateBack = onNavigateBack,
        )
    }
}

internal fun NavController.navigateToWatchlistReorder() {
    navigate(route = WatchlistReorderDestination)
}
