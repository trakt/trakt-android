package tv.trakt.trakt.core.home.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.common.helpers.extensions.popUpToTop
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.home.HomeScreen

@Serializable
internal data object HomeDestination

internal fun NavGraphBuilder.homeScreen(
    userLoading: Boolean,
    onNavigateToProfile: () -> Unit,
    onNavigateToShow: (TraktId) -> Unit,
    onNavigateToShows: () -> Unit,
    onNavigateToMovies: () -> Unit,
    onNavigateToMovie: (TraktId) -> Unit,
    onNavigateToEpisode: (showId: TraktId, episode: Episode) -> Unit,
    onNavigateToAllUpNext: () -> Unit,
    onNavigateToAllWatchlist: () -> Unit,
    onNavigateToAllPersonal: () -> Unit,
    onNavigateToAllSocial: () -> Unit,
) {
    composable<HomeDestination> {
        HomeScreen(
            viewModel = koinViewModel(),
            userLoading = userLoading,
            onNavigateToProfile = onNavigateToProfile,
            onNavigateToShow = onNavigateToShow,
            onNavigateToShows = onNavigateToShows,
            onNavigateToMovies = onNavigateToMovies,
            onNavigateToMovie = onNavigateToMovie,
            onNavigateToEpisode = onNavigateToEpisode,
            onNavigateToAllUpNext = onNavigateToAllUpNext,
            onNavigateToAllWatchlist = onNavigateToAllWatchlist,
            onNavigateToAllPersonal = onNavigateToAllPersonal,
            onNavigateToAllSocial = onNavigateToAllSocial,
        )
    }
}

internal fun NavController.navigateToHome() {
    navigate(route = HomeDestination) {
        popUpToTop(this@navigateToHome)
    }
}
