package tv.trakt.trakt.core.lists.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import tv.trakt.trakt.common.helpers.extensions.popUpToTop
import tv.trakt.trakt.core.lists.ListsScreen
import tv.trakt.trakt.tv.common.model.TraktId

@Serializable
internal data object ListsDestination

internal fun NavGraphBuilder.listsScreen(onNavigateToList: (TraktId) -> Unit) {
    composable<ListsDestination> {
        ListsScreen(
            onNavigateToList = onNavigateToList,
        )
    }
}

internal fun NavController.navigateToLists() {
    navigate(route = ListsDestination) {
        popUpToTop(this@navigateToLists)
    }
}
