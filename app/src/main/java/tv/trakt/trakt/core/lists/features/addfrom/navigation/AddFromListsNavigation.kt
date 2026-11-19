package tv.trakt.trakt.core.lists.features.addfrom.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.common.model.lists.CustomList
import tv.trakt.trakt.core.lists.features.addfrom.AddFromListsScreen

@Serializable
internal data class AddFromListsDestination(
    val listJson: String,
)

internal fun NavGraphBuilder.addFromListsScreen(
    onNavigateBack: () -> Unit,
    onShowMessage: (String) -> Unit,
) {
    composable<AddFromListsDestination> {
        AddFromListsScreen(
            viewModel = koinViewModel(),
            onNavigateBack = onNavigateBack,
            onShowMessage = onShowMessage,
        )
    }
}

internal fun NavController.navigateToAddFromLists(list: CustomList) {
    navigate(
        route = AddFromListsDestination(
            listJson = Json.encodeToString(list),
        ),
    )
}
