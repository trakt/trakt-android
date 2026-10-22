package tv.trakt.trakt.core.lists.sections.watchlist.features.all.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.lists.sections.watchlist.features.all.AllWatchlistScreen

@Serializable
internal data object ListsWatchlistDestination

internal fun NavGraphBuilder.allWatchlistScreen(
    onNavigateToShow: (TraktId) -> Unit,
    onNavigateToMovie: (TraktId) -> Unit,
    onNavigateToReorder: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    composable<ListsWatchlistDestination> {
        AllWatchlistScreen(
            viewModel = koinViewModel(),
            onShowClick = onNavigateToShow,
            onMovieClick = onNavigateToMovie,
            onReorderClick = onNavigateToReorder,
            onNavigateBack = onNavigateBack,
        )
    }
}

internal fun NavController.navigateToWatchlist() {
    navigate(route = ListsWatchlistDestination)
}
