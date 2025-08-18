package tv.trakt.trakt.app.core.home.sections.movies.comingsoon.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.app.common.model.TraktId
import tv.trakt.trakt.app.core.home.sections.movies.comingsoon.viewall.ComingSoonViewAllScreen

@Serializable
internal data object HomeComingSoonDestination

internal fun NavGraphBuilder.homeComingSoonScreen(onNavigateToMovie: (TraktId) -> Unit) {
    composable<HomeComingSoonDestination> {
        ComingSoonViewAllScreen(
            viewModel = koinViewModel(),
            onNavigateToMovie = onNavigateToMovie,
        )
    }
}

internal fun NavController.navigateToHomeComingSoon() {
    navigate(route = HomeComingSoonDestination)
}
