package tv.trakt.trakt.core.lists.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.common.helpers.extensions.popUpToTop
import tv.trakt.trakt.common.model.CustomList
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.lists.ListsScreen

@Serializable
internal data object ListsDestination

internal fun NavGraphBuilder.listsScreen(
    onNavigateToProfile: () -> Unit,
    onNavigateToShow: (TraktId) -> Unit,
    onNavigateToDiscover: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToMovie: (TraktId) -> Unit,
    onNavigateToWatchlist: () -> Unit,
    onNavigateToPersonalList: (CustomList) -> Unit,
    onNavigateToCustomList: (CustomList) -> Unit,
    onNavigateToVip: () -> Unit,
) {
    composable<ListsDestination> {
        ListsScreen(
            viewModel = koinViewModel(),
            onNavigateToProfile = onNavigateToProfile,
            onNavigateToShow = onNavigateToShow,
            onNavigateToDiscover = onNavigateToDiscover,
            onNavigateToSearch = onNavigateToSearch,
            onNavigateToMovie = onNavigateToMovie,
            onNavigateToWatchlist = onNavigateToWatchlist,
            onNavigateToPersonalList = onNavigateToPersonalList,
            onNavigateToCustomList = onNavigateToCustomList,
            onNavigateToVip = onNavigateToVip,
        )
    }
}

internal fun NavController.navigateToLists() {
    navigate(route = ListsDestination) {
        popUpToTop(this@navigateToLists)
    }
}
