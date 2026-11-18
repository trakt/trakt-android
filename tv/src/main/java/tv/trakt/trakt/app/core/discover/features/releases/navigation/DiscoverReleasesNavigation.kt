package tv.trakt.trakt.app.core.discover.features.releases.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.app.core.discover.features.releases.DiscoverReleasesViewAllScreen
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.TraktId

@Serializable
internal data object DiscoverReleasesDestination

internal fun NavGraphBuilder.discoverReleasesScreen(
    onNavigateToMovie: (TraktId) -> Unit,
    onNavigateToEpisode: (showId: TraktId, episode: Episode) -> Unit,
) {
    composable<DiscoverReleasesDestination> {
        DiscoverReleasesViewAllScreen(
            viewModel = koinViewModel(),
            onNavigateToMovie = onNavigateToMovie,
            onNavigateToEpisode = onNavigateToEpisode,
        )
    }
}

internal fun NavController.navigateToDiscoverReleases() {
    navigate(route = DiscoverReleasesDestination)
}
