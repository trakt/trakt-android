package tv.trakt.trakt.core.lists.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.common.helpers.extensions.popUpToTop
import tv.trakt.trakt.common.model.CustomList
import tv.trakt.trakt.core.lists.ListsScreen

@Serializable
internal data object ListsDestination

internal fun NavGraphBuilder.listsScreen(
    onNavigateToProfile: () -> Unit,
    onNavigateToShows: () -> Unit,
    onNavigateToMovies: () -> Unit,
    onNavigateToWatchlist: () -> Unit,
    onNavigateToList: (CustomList) -> Unit,
) {
    composable<ListsDestination> {
        ListsScreen(
            viewModel = koinViewModel(),
            onNavigateToProfile = onNavigateToProfile,
            onNavigateToShows = onNavigateToShows,
            onNavigateToMovies = onNavigateToMovies,
            onNavigateToWatchlist = onNavigateToWatchlist,
            onNavigateToList = onNavigateToList,
        )
    }
}

internal fun NavController.navigateToLists() {
    navigate(route = ListsDestination) {
        popUpToTop(this@navigateToLists)
    }
}
