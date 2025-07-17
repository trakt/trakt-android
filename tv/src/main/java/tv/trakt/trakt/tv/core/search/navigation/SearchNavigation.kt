package tv.trakt.trakt.tv.core.search.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import tv.trakt.trakt.tv.core.search.SearchScreen
import tv.trakt.trakt.tv.helpers.extensions.popUpToTop

@Serializable
internal data object SearchDestination

internal fun NavGraphBuilder.searchScreen() {
    composable<SearchDestination> {
        SearchScreen()
    }
}

internal fun NavController.navigateToSearch() {
    navigate(route = SearchDestination) {
        popUpToTop(this@navigateToSearch)
    }
}
