package tv.trakt.trakt.app.core.lists.details.media.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.app.core.lists.details.media.MediaWatchlistScreen
import tv.trakt.trakt.common.model.TraktId

@Serializable
internal data object WatchlistMediaDestination

internal fun NavGraphBuilder.watchlistMediaScreen(
    onNavigateToShow: (TraktId) -> Unit,
    onNavigateToMovie: (TraktId) -> Unit,
) {
    composable<WatchlistMediaDestination> {
        MediaWatchlistScreen(
            viewModel = koinViewModel(),
            onNavigateToShow = onNavigateToShow,
            onNavigateToMovie = onNavigateToMovie,
        )
    }
}

internal fun NavController.navigateToWatchlistMedia() {
    navigate(
        route = WatchlistMediaDestination,
    )
}
