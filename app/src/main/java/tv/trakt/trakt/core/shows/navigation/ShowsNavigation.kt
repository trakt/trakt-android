package tv.trakt.trakt.core.shows.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.common.helpers.extensions.popUpToTop
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.shows.ShowsScreen

@Serializable
internal data object ShowsDestination

internal fun NavGraphBuilder.showsScreen(
    onNavigateToShow: (TraktId) -> Unit,
    onNavigateToProfile: () -> Unit,
) {
    composable<ShowsDestination> {
        ShowsScreen(
            viewModel = koinViewModel(),
            onNavigateToShow = onNavigateToShow,
            onNavigateToProfile = onNavigateToProfile,
        )
    }
}

internal fun NavController.navigateToShows() {
    navigate(route = ShowsDestination) {
        popUpToTop(this@navigateToShows)
    }
}
