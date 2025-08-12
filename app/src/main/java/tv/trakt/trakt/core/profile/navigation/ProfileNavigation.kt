package tv.trakt.trakt.core.profile.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.core.profile.ProfileScreen

@Serializable
internal data object ProfileDestination

internal fun NavGraphBuilder.profileScreen(onNavigateBack: () -> Unit) {
    composable<ProfileDestination> {
        ProfileScreen(
            viewModel = koinViewModel(),
            onNavigateBack = onNavigateBack,
        )
    }
}

internal fun NavController.navigateToProfile() {
    navigate(route = ProfileDestination)
}
