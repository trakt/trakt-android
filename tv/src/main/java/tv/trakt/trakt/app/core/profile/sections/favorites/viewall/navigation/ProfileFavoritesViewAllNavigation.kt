package tv.trakt.trakt.app.core.profile.sections.favorites.viewall.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.app.core.profile.sections.favorites.viewall.ProfileFavoritesViewAllScreen
import tv.trakt.trakt.common.model.TraktId

@Serializable
internal data object ProfileFavoritesViewAllDestination

internal fun NavGraphBuilder.profileFavoritesViewAllScreen(
    onNavigateToShow: (TraktId) -> Unit,
    onNavigateToMovie: (TraktId) -> Unit,
) {
    composable<ProfileFavoritesViewAllDestination> {
        ProfileFavoritesViewAllScreen(
            viewModel = koinViewModel(),
            onNavigateToShow = onNavigateToShow,
            onNavigateToMovie = onNavigateToMovie,
        )
    }
}

internal fun NavController.navigateToProfileFavoritesViewAll() {
    navigate(
        route = ProfileFavoritesViewAllDestination,
    )
}
