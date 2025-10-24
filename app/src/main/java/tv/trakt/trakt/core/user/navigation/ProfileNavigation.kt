package tv.trakt.trakt.core.user.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.user.features.profile.ProfileScreen

@Serializable
internal data object ProfileDestination

internal fun NavGraphBuilder.profileScreen(
    onNavigateToShow: (TraktId) -> Unit,
    onNavigateToMovie: (TraktId) -> Unit,
    onNavigateToFavorites: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    composable<ProfileDestination> {
        ProfileScreen(
            viewModel = koinViewModel(),
            onNavigateToShow = onNavigateToShow,
            onNavigateToMovie = onNavigateToMovie,
            onNavigateToFavorites = onNavigateToFavorites,
            onNavigateBack = onNavigateBack,
        )
    }
}

internal fun NavController.navigateToProfile() {
    navigate(route = ProfileDestination)
}
