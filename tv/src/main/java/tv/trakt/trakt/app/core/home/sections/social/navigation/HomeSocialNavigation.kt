package tv.trakt.trakt.app.core.home.sections.social.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.app.common.model.TraktId
import tv.trakt.trakt.app.core.episodes.model.Episode
import tv.trakt.trakt.app.core.home.sections.social.viewall.SocialViewAllScreen

@Serializable
internal data object HomeSocialDestination

internal fun NavGraphBuilder.homeSocialScreen(
    onNavigateToEpisode: (TraktId, Episode) -> Unit,
    onNavigateToMovie: (TraktId) -> Unit,
) {
    composable<HomeSocialDestination> {
        SocialViewAllScreen(
            viewModel = koinViewModel(),
            onNavigateToEpisode = onNavigateToEpisode,
            onNavigateToMovie = onNavigateToMovie,
        )
    }
}

internal fun NavController.navigateToHomeSocial() {
    navigate(route = HomeSocialDestination)
}
