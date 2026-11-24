package tv.trakt.trakt.core.lists.features.smart.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.common.model.lists.SmartList
import tv.trakt.trakt.common.model.lists.SmartListFilters
import tv.trakt.trakt.core.lists.features.smart.CreateSmartListScreen

@Serializable
internal data class CreateSmartListDestination(
    val listJson: String? = null,
    val filtersJson: String? = null,
    val name: String? = null,
)

internal fun NavGraphBuilder.createSmartListScreen(onNavigateBack: () -> Unit) {
    composable<CreateSmartListDestination> {
        CreateSmartListScreen(
            viewModel = koinViewModel(),
            onNavigateBack = onNavigateBack,
        )
    }
}

internal fun NavController.navigateToCreateSmartList(
    filters: SmartListFilters? = null,
    name: String? = null,
) {
    navigate(
        route = CreateSmartListDestination(
            filtersJson = filters?.let { Json.encodeToString(it) },
            name = name,
        ),
    )
}

internal fun NavController.navigateToEditSmartList(list: SmartList) {
    navigate(
        route = CreateSmartListDestination(
            listJson = Json.encodeToString(list),
        ),
    )
}
