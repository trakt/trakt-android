package tv.trakt.trakt.core.profile.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.profile.ProfileScreen

@Serializable
internal data object ProfileDestination

internal fun NavGraphBuilder.profileScreen(
    onNavigateToShow: (TraktId) -> Unit,
    onNavigateToMovie: (TraktId) -> Unit,
    onNavigateToEpisode: (showId: TraktId, episode: Episode) -> Unit,
    onNavigateToFavorites: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToDiscover: () -> Unit,
    onNavigateToSettings: () -> Unit,
) {
    composable<ProfileDestination> {
        ProfileScreen(
            viewModel = koinViewModel(),
            onNavigateToShow = onNavigateToShow,
            onNavigateToMovie = onNavigateToMovie,
            onNavigateToEpisode = onNavigateToEpisode,
            onNavigateToFavorites = onNavigateToFavorites,
            onNavigateToHistory = onNavigateToHistory,
            onNavigateToDiscover = onNavigateToDiscover,
            onNavigateToSettings = onNavigateToSettings,
        )
    }
}

internal fun NavController.navigateToProfile() {
    navigate(route = ProfileDestination)
}
