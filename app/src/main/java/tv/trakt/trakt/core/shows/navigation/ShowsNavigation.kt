package tv.trakt.trakt.core.shows.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import tv.trakt.trakt.common.helpers.extensions.popUpToTop
import tv.trakt.trakt.core.shows.ShowsScreen
import tv.trakt.trakt.tv.common.model.TraktId

@Serializable
internal data object ShowsDestination

internal fun NavGraphBuilder.showsScreen(onNavigateToShow: (TraktId) -> Unit) {
    composable<ShowsDestination> {
        ShowsScreen(
            onNavigateToShow = onNavigateToShow,
        )
    }
}

internal fun NavController.navigateToShows() {
    navigate(route = ShowsDestination) {
        popUpToTop(this@navigateToShows)
    }
}
