package tv.trakt.trakt.app.core.lists.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.app.core.lists.ListsScreen
import tv.trakt.trakt.common.helpers.extensions.popUpToTop
import tv.trakt.trakt.common.model.CustomList
import tv.trakt.trakt.common.model.TraktId

@Serializable
internal data object ListsDestination

internal fun NavGraphBuilder.listsScreen(
    onNavigateToList: (CustomList) -> Unit,
    onNavigateToShow: (TraktId) -> Unit,
    onNavigateToMovie: (TraktId) -> Unit,
    onNavigateToWatchlistShow: () -> Unit,
    onNavigateToWatchlistMovie: () -> Unit,
) {
    composable<ListsDestination> {
        ListsScreen(
            viewModel = koinViewModel(),
            onListClick = onNavigateToList,
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
