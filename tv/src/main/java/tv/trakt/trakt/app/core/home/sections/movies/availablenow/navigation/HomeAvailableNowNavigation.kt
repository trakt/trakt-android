package tv.trakt.trakt.app.core.home.sections.movies.availablenow.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.app.common.model.TraktId
import tv.trakt.trakt.app.core.home.sections.movies.availablenow.viewall.AvailableNowViewAllScreen

@Serializable
internal data object HomeAvailableNowDestination

internal fun NavGraphBuilder.homeAvailableNowScreen(onNavigateToMovie: (TraktId) -> Unit) {
    composable<HomeAvailableNowDestination> {
        AvailableNowViewAllScreen(
            viewModel = koinViewModel(),
            onNavigateToMovie = onNavigateToMovie,
        )
    }
}

internal fun NavController.navigateToHomeAvailableNow() {
    navigate(route = HomeAvailableNowDestination)
}
