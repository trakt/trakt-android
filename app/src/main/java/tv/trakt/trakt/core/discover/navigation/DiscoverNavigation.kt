package tv.trakt.trakt.core.discover.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.common.helpers.extensions.popUpToTop
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.discover.DiscoverScreen

@Serializable
internal data object DiscoverDestination

internal fun NavGraphBuilder.discoverScreen(
    onNavigateToShow: (TraktId) -> Unit,
    onNavigateToMovie: (TraktId) -> Unit,
    onNavigateToEpisode: (showId: TraktId, episode: Episode) -> Unit,
    onNavigateToAllTrending: () -> Unit = {},
    onNavigateToAllPopular: () -> Unit = {},
    onNavigateToAllAnticipated: () -> Unit = {},
    onNavigateToAllReleases: () -> Unit = {},
    onNavigateToVip: () -> Unit = {},
) {
    composable<DiscoverDestination> {
        DiscoverScreen(
            viewModel = koinViewModel(),
            onNavigateToShow = onNavigateToShow,
            onNavigateToMovie = onNavigateToMovie,
            onNavigateToEpisode = onNavigateToEpisode,
            onNavigateToAllTrending = onNavigateToAllTrending,
            onNavigateToAllPopular = onNavigateToAllPopular,
            onNavigateToAllAnticipated = onNavigateToAllAnticipated,
            onNavigateToAllReleases = onNavigateToAllReleases,
            onNavigateToVip = onNavigateToVip,
        )
    }
}

internal fun NavController.navigateToDiscover() {
    navigate(route = DiscoverDestination) {
        popUpToTop(this@navigateToDiscover)
    }
}
