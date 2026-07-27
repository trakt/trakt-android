package tv.trakt.trakt.app.core.shows.features.releases.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.app.core.shows.features.releases.ShowsReleasesScreen
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.TraktId

@Serializable
internal data object ShowsReleasesDestination

internal fun NavGraphBuilder.showsReleasesScreen(onNavigateToEpisode: (showId: TraktId, episode: Episode) -> Unit) {
    composable<ShowsReleasesDestination> {
        ShowsReleasesScreen(
            viewModel = koinViewModel(),
            onNavigateToEpisode = onNavigateToEpisode,
        )
    }
}

internal fun NavController.navigateToShowsReleases() {
    navigate(route = ShowsReleasesDestination)
}
