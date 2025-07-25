package tv.trakt.trakt.tv.core.profile.sections.favorites.shows.viewall.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.tv.core.profile.sections.favorites.shows.viewall.ProfileFavoriteShowsViewAllScreen

@Serializable
internal data object ProfileFavoriteShowsViewAllDestination

internal fun NavGraphBuilder.profileFavoriteShowsViewAllScreen(onNavigateToShow: (TraktId) -> Unit) {
    composable<ProfileFavoriteShowsViewAllDestination> {
        ProfileFavoriteShowsViewAllScreen(
            viewModel = koinViewModel(),
            onNavigateToShow = onNavigateToShow,
        )
    }
}

internal fun NavController.navigateToProfileFavoriteShowsViewAll() {
    navigate(
        route = ProfileFavoriteShowsViewAllDestination,
    )
}
