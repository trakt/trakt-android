package tv.trakt.trakt.core.discover.sections.releases.all.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.discover.sections.releases.all.AllReleasesScreen

@Serializable
internal data object AllReleasesDestination

internal fun NavGraphBuilder.allReleasesScreen(
    onNavigateBack: () -> Unit,
    onEpisodeClick: (showId: TraktId, episode: Episode) -> Unit,
    onShowClick: (TraktId) -> Unit,
    onMovieClick: (TraktId) -> Unit,
) {
    composable<AllReleasesDestination> {
        AllReleasesScreen(
            viewModel = koinViewModel(),
            onEpisodeClick = onEpisodeClick,
            onShowClick = onShowClick,
            onMovieClick = onMovieClick,
            onNavigateBack = onNavigateBack,
        )
    }
}

internal fun NavController.navigateToAllReleases() {
    navigate(route = AllReleasesDestination)
}
