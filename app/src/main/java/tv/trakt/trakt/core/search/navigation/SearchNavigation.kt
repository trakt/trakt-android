package tv.trakt.trakt.core.search.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import tv.trakt.trakt.common.helpers.extensions.popUpToTop
import tv.trakt.trakt.core.search.SearchScreen
import tv.trakt.trakt.tv.common.model.TraktId

@Serializable
internal data object SearchDestination

internal fun NavGraphBuilder.searchScreen(onNavigateToSearch: (TraktId) -> Unit) {
    composable<SearchDestination> {
        SearchScreen(
            onNavigateToSearch = onNavigateToSearch,
        )
    }
}

internal fun NavController.navigateToSearch() {
    navigate(route = SearchDestination) {
        popUpToTop(this@navigateToSearch)
    }
}
