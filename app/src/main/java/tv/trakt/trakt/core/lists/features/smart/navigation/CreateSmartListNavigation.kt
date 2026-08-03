package tv.trakt.trakt.core.lists.features.smart.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.core.lists.features.smart.CreateSmartListScreen

@Serializable
internal data object CreateSmartListDestination

internal fun NavGraphBuilder.createSmartListScreen(onNavigateBack: () -> Unit) {
    composable<CreateSmartListDestination> {
        CreateSmartListScreen(
            viewModel = koinViewModel(),
            onNavigateBack = onNavigateBack,
        )
    }
}

internal fun NavController.navigateToCreateSmartList() {
    navigate(route = CreateSmartListDestination)
}
