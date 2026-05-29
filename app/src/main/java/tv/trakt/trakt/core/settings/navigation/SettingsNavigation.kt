package tv.trakt.trakt.core.settings.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.core.settings.SettingsScreen
import tv.trakt.trakt.core.settings.features.blocked.BlockedUsersScreen

@Serializable
internal data object SettingsDestination

@Serializable
internal data object BlockedUsersDestination

internal fun NavGraphBuilder.settingsScreen(
    onNavigateHome: () -> Unit,
    onNavigateYounify: () -> Unit,
    onNavigateBlockedUsers: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    composable<SettingsDestination> {
        SettingsScreen(
            viewModel = koinViewModel(),
            onNavigateHome = onNavigateHome,
            onNavigateYounify = onNavigateYounify,
            onNavigateBlockedUsers = onNavigateBlockedUsers,
            onNavigateBack = onNavigateBack,
        )
    }

    composable<BlockedUsersDestination> {
        BlockedUsersScreen(
            viewModel = koinViewModel(),
            onNavigateBack = onNavigateBack,
        )
    }
}

internal fun NavController.navigateToSettings() {
    navigate(route = SettingsDestination)
}

internal fun NavController.navigateToBlockedUsers() {
    navigate(route = BlockedUsersDestination)
}
