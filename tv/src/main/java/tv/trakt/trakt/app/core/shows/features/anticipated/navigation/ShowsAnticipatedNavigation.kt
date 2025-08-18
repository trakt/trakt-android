package tv.trakt.trakt.app.core.shows.features.anticipated.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.app.core.shows.features.anticipated.ShowsAnticipatedScreen
import tv.trakt.trakt.common.model.TraktId

@Serializable
internal data object ShowsAnticipatedDestination

internal fun NavGraphBuilder.showsAnticipatedScreen(onNavigateToShow: (TraktId) -> Unit) {
    composable<ShowsAnticipatedDestination> {
        ShowsAnticipatedScreen(
            viewModel = koinViewModel(),
            onNavigateToShow = onNavigateToShow,
        )
    }
}

internal fun NavController.navigateToShowsAnticipated() {
    navigate(route = ShowsAnticipatedDestination)
}
