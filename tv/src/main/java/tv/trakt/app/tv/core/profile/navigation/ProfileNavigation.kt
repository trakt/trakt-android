package tv.trakt.app.tv.core.profile.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.app.tv.common.model.TraktId
import tv.trakt.app.tv.core.episodes.model.Episode
import tv.trakt.app.tv.core.profile.ProfileScreen
import tv.trakt.app.tv.helpers.extensions.popUpToTop

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
