package tv.trakt.trakt.tv.core.profile.sections.favorites.movies.viewall.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.tv.common.model.TraktId
import tv.trakt.trakt.tv.core.profile.sections.favorites.movies.viewall.ProfileFavoriteMoviesViewAllScreen

@Serializable
internal data object ProfileFavoriteMoviesViewAllDestination

internal fun NavGraphBuilder.profileFavoriteMoviesViewAllScreen(onNavigateToMovie: (TraktId) -> Unit) {
    composable<ProfileFavoriteMoviesViewAllDestination> {
        ProfileFavoriteMoviesViewAllScreen(
            viewModel = koinViewModel(),
            onNavigateToMovie = onNavigateToMovie,
        )
    }
}

internal fun NavController.navigateToProfileFavoriteMoviesViewAll() {
    navigate(
        route = ProfileFavoriteMoviesViewAllDestination,
    )
}
