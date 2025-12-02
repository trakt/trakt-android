package tv.trakt.trakt.core.settings.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.core.settings.SettingsScreen

@Serializable
internal data object SettingsDestination

internal fun NavGraphBuilder.settingsScreen(
    onNavigateHome: () -> Unit,
    onNavigateYounify: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    composable<SettingsDestination> {
        SettingsScreen(
            viewModel = koinViewModel(),
            onNavigateHome = onNavigateHome,
            onNavigateYounify = onNavigateYounify,
            onNavigateBack = onNavigateBack,
        )
    }
}

internal fun NavController.navigateToSettings() {
    navigate(route = SettingsDestination)
}
