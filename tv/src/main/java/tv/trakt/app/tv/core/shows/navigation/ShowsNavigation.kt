package tv.trakt.app.tv.core.shows.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.app.tv.common.model.TraktId
import tv.trakt.app.tv.core.shows.ShowsScreen
import tv.trakt.app.tv.helpers.extensions.popUpToTop

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
