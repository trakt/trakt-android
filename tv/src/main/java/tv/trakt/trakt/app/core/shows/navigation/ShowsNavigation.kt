package tv.trakt.trakt.app.core.shows.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.app.core.shows.ShowsScreen
import tv.trakt.trakt.common.helpers.extensions.popUpToTop
import tv.trakt.trakt.common.model.TraktId

@Serializable
internal data object ShowsDestination

internal fun NavGraphBuilder.showsScreen(onNavigateToShow: (TraktId) -> Unit) {
    composable<ShowsDestination> {
        ShowsScreen(
            viewModel = koinViewModel(),
            onNavigateToShow = onNavigateToShow,
        )
    }
}

internal fun NavController.navigateToShows() {
    navigate(route = ShowsDestination) {
        popUpToTop(this@navigateToShows)
    }
}
