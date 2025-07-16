package tv.trakt.app.tv.core.lists.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.app.tv.common.model.TraktId
import tv.trakt.app.tv.core.lists.ListsScreen
import tv.trakt.app.tv.helpers.extensions.popUpToTop

@Serializable
internal data object ListsDestination

internal fun NavGraphBuilder.listsScreen(
    onNavigateToShow: (TraktId) -> Unit,
    onNavigateToMovie: (TraktId) -> Unit,
    onNavigateToWatchlistShow: () -> Unit,
    onNavigateToWatchlistMovie: () -> Unit,
) {
    composable<ListsDestination> {
        ListsScreen(
            viewModel = koinViewModel(),
            onShowClick = onNavigateToShow,
            onMovieClick = onNavigateToMovie,
            onShowViewAllClick = onNavigateToWatchlistShow,
            onMovieViewAllClick = onNavigateToWatchlistMovie,
        )
    }
}

internal fun NavController.navigateToLists() {
    navigate(route = ListsDestination) {
        popUpToTop(this@navigateToLists)
    }
}
