package tv.trakt.trakt.core.settings.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.core.settings.SettingsScreen
import tv.trakt.trakt.core.settings.categories.account.AccountSettingsScreen
import tv.trakt.trakt.core.settings.categories.appearance.AppearanceSettingsScreen
import tv.trakt.trakt.core.settings.categories.community.CommunitySettingsScreen
import tv.trakt.trakt.core.settings.categories.general.GeneralSettingsScreen
import tv.trakt.trakt.core.settings.categories.notifications.NotificationSettingsScreen
import tv.trakt.trakt.core.settings.categories.streaming.StreamingSettingsScreen
import tv.trakt.trakt.core.settings.categories.streaming.StreamingSettingsViewModel
import tv.trakt.trakt.core.settings.features.blocked.BlockedUsersScreen
import tv.trakt.trakt.core.settings.model.SettingsCategory

@Serializable
internal data object SettingsDestination

@Serializable
internal data object GeneralSettingsDestination

@Serializable
internal data object AccountSettingsDestination

@Serializable
internal data object AppearanceSettingsDestination

@Serializable
internal data object StreamingSettingsDestination

@Serializable
internal data object NotificationSettingsDestination

@Serializable
internal data object CommunitySettingsDestination

@Serializable
internal data object BlockedUsersDestination

internal fun NavGraphBuilder.settingsScreen(
    onNavigateHome: () -> Unit,
    onNavigateCategory: (SettingsCategory) -> Unit,
    onNavigateYounify: () -> Unit,
    onNavigateBlockedUsers: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    composable<SettingsDestination> {
        SettingsScreen(
            viewModel = koinViewModel(),
            onNavigateCategory = onNavigateCategory,
            onNavigateBack = onNavigateBack,
        )
    }

    composable<GeneralSettingsDestination> {
        GeneralSettingsScreen(
            viewModel = koinViewModel(),
            onNavigateHome = onNavigateHome,
            onNavigateBlockedUsers = onNavigateBlockedUsers,
            onNavigateBack = onNavigateBack,
        )
    }

    composable<AccountSettingsDestination> {
        AccountSettingsScreen(
            viewModel = koinViewModel(),
            onNavigateBack = onNavigateBack,
        )
    }

    composable<AppearanceSettingsDestination> {
        AppearanceSettingsScreen(
            viewModel = koinViewModel(),
            onNavigateBack = onNavigateBack,
        )
    }

    composable<StreamingSettingsDestination> {
        koinViewModel<StreamingSettingsViewModel>()
        StreamingSettingsScreen(
            onNavigateYounify = onNavigateYounify,
            onNavigateBack = onNavigateBack,
        )
    }

    composable<NotificationSettingsDestination> {
        NotificationSettingsScreen(
            viewModel = koinViewModel(),
            onNavigateBack = onNavigateBack,
        )
    }

    composable<CommunitySettingsDestination> {
        CommunitySettingsScreen(
            viewModel = koinViewModel(),
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

internal fun NavController.navigateToSettingsCategory(category: SettingsCategory) {
    val route: Any = when (category) {
        SettingsCategory.General -> GeneralSettingsDestination
        SettingsCategory.Account -> AccountSettingsDestination
        SettingsCategory.Appearance -> AppearanceSettingsDestination
        SettingsCategory.Streaming -> StreamingSettingsDestination
        SettingsCategory.Notifications -> NotificationSettingsDestination
        SettingsCategory.Support -> CommunitySettingsDestination
    }
    navigate(route = route)
}

internal fun NavController.navigateToBlockedUsers() {
    navigate(route = BlockedUsersDestination)
}
