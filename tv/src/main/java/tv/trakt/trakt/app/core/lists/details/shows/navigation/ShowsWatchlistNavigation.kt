package tv.trakt.trakt.app.core.lists.details.shows.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.app.core.lists.details.shows.ShowsWatchlistScreen
import tv.trakt.trakt.common.model.TraktId

@Serializable
internal data object WatchlistShowsDestination

internal fun NavGraphBuilder.watchlistShowsScreen(onNavigateToShow: (TraktId) -> Unit) {
    composable<WatchlistShowsDestination> {
        ShowsWatchlistScreen(
            viewModel = koinViewModel(),
            onNavigateToShow = onNavigateToShow,
        )
    }
}

internal fun NavController.navigateToWatchlistShows() {
    navigate(
        route = WatchlistShowsDestination,
    )
}
