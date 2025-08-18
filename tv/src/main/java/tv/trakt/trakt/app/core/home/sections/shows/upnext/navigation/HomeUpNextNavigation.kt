package tv.trakt.trakt.app.core.home.sections.shows.upnext.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.app.core.episodes.model.Episode
import tv.trakt.trakt.app.core.home.sections.shows.upnext.viewall.UpNextViewAllScreen
import tv.trakt.trakt.common.model.TraktId

@Serializable
internal data object HomeUpNextDestination

internal fun NavGraphBuilder.homeUpNextScreen(onNavigateToEpisode: (showId: TraktId, episode: Episode) -> Unit) {
    composable<HomeUpNextDestination> {
        UpNextViewAllScreen(
            viewModel = koinViewModel(),
            onNavigateToEpisode = onNavigateToEpisode,
        )
    }
}

internal fun NavController.navigateToHomeUpNext() {
    navigate(route = HomeUpNextDestination)
}
