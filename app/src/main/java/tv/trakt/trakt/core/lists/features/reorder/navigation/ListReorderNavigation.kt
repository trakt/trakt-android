package tv.trakt.trakt.core.lists.features.reorder.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.common.model.lists.CustomList
import tv.trakt.trakt.core.lists.features.reorder.ListReorderScreen

@Serializable
internal data class ListReorderDestination(
    val listJson: String,
)

internal fun NavGraphBuilder.listReorderScreen(onNavigateBack: () -> Unit) {
    composable<ListReorderDestination> {
        ListReorderScreen(
            viewModel = koinViewModel(),
            onNavigateBack = onNavigateBack,
        )
    }
}

internal fun NavController.navigateToListReorder(list: CustomList) {
    navigate(
        route = ListReorderDestination(
            listJson = Json.encodeToString(list),
        ),
    )
}
