package tv.trakt.trakt.core.home.sections.upnext.features.all.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.home.sections.upnext.features.all.AllHomeUpNextScreen

@Serializable
internal data object HomeUpNextDestination

internal fun NavGraphBuilder.homeUpNextScreen(
    onNavigateToShow: (TraktId) -> Unit,
    onNavigateToEpisode: (showId: TraktId, episode: Episode) -> Unit,
    onNavigateBack: () -> Unit,
) {
    composable<HomeUpNextDestination> {
        AllHomeUpNextScreen(
            viewModel = koinViewModel(),
            onNavigateToShow = onNavigateToShow,
            onNavigateToEpisode = onNavigateToEpisode,
            onNavigateBack = onNavigateBack,
        )
    }
}

internal fun NavController.navigateToAllUpNext() {
    navigate(route = HomeUpNextDestination)
}
