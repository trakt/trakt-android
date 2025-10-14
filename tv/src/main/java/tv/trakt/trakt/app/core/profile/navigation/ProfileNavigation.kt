package tv.trakt.trakt.app.core.profile.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.app.core.profile.ProfileScreen
import tv.trakt.trakt.common.helpers.extensions.popUpToTop
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.TraktId

@Serializable
internal data object ProfileDestination

internal fun NavGraphBuilder.profileScreen(
    onNavigateToMovie: (TraktId) -> Unit,
    onNavigateToShow: (TraktId) -> Unit,
    onNavigateToEpisode: (showId: TraktId, episode: Episode) -> Unit,
    onNavigateToHistoryViewAll: () -> Unit,
    onNavigateToFavShowsViewAll: () -> Unit,
    onNavigateToFavMoviesViewAll: () -> Unit,
) {
    composable<ProfileDestination> {
        ProfileScreen(
            viewModel = koinViewModel(),
            onNavigateToMovie = onNavigateToMovie,
            onNavigateToShow = onNavigateToShow,
            onNavigateToEpisode = onNavigateToEpisode,
            onNavigateToHistoryViewAll = onNavigateToHistoryViewAll,
            onNavigateToFavShowsViewAll = onNavigateToFavShowsViewAll,
            onNavigateToFavMoviesViewAll = onNavigateToFavMoviesViewAll,
        )
    }
}

internal fun NavController.navigateToProfile() {
    navigate(route = ProfileDestination) {
        popUpToTop(this@navigateToProfile)
    }
}
