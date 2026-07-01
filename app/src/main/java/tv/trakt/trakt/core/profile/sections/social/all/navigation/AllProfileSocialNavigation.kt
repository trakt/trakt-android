package tv.trakt.trakt.core.profile.sections.social.all.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.core.profile.sections.social.all.AllProfileSocialScreen

@Serializable
internal data class AllProfileSocialDestination(
    val unused: Boolean = false,
)

internal fun NavGraphBuilder.allProfileSocialScreen(
    onNavigateToUser: (User) -> Unit,
    onNavigateBack: () -> Unit,
) {
    composable<AllProfileSocialDestination> {
        AllProfileSocialScreen(
            viewModel = koinViewModel(),
            onUserClick = onNavigateToUser,
            onNavigateBack = onNavigateBack,
        )
    }
}

internal fun NavController.navigateToProfileSocial() {
    navigate(route = AllProfileSocialDestination())
}
