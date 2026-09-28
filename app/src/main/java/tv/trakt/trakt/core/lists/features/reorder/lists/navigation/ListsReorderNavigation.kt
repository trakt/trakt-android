package tv.trakt.trakt.core.lists.features.reorder.lists.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.core.lists.features.reorder.lists.ListsReorderScreen

@Serializable
internal data object ListsReorderDestination

internal fun NavGraphBuilder.listsReorderScreen(onNavigateBack: () -> Unit) {
    composable<ListsReorderDestination> {
        ListsReorderScreen(
            viewModel = koinViewModel(),
            onNavigateBack = onNavigateBack,
        )
    }
}

internal fun NavController.navigateToListsReorder() {
    navigate(route = ListsReorderDestination)
}
