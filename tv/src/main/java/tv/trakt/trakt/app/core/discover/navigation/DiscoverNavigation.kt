package tv.trakt.trakt.app.core.discover.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.app.core.discover.DiscoverScreen
import tv.trakt.trakt.common.helpers.extensions.popUpToTop
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.MediaMode
import tv.trakt.trakt.common.model.TraktId

@Serializable
internal data object DiscoverDestination

internal fun NavGraphBuilder.discoverScreen(
    onNavigateToShow: (TraktId) -> Unit,
    onNavigateToMovie: (TraktId) -> Unit,
    onNavigateToEpisode: (showId: TraktId, episode: Episode) -> Unit,
    onNavigateToTrending: (MediaMode) -> Unit,
    onNavigateToReleases: (MediaMode) -> Unit,
    onNavigateToPopular: (MediaMode) -> Unit,
    onNavigateToAnticipated: (MediaMode) -> Unit,
) {
    composable<DiscoverDestination> {
        DiscoverScreen(
            viewModel = koinViewModel(),
            onNavigateToShow = onNavigateToShow,
            onNavigateToMovie = onNavigateToMovie,
            onNavigateToEpisode = onNavigateToEpisode,
            onNavigateToTrending = onNavigateToTrending,
            onNavigateToReleases = onNavigateToReleases,
            onNavigateToPopular = onNavigateToPopular,
            onNavigateToAnticipated = onNavigateToAnticipated,
        )
    }
}

internal fun NavController.navigateToDiscover() {
    navigate(route = DiscoverDestination) {
        popUpToTop(this@navigateToDiscover)
    }
}
