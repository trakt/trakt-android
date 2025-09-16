package tv.trakt.trakt.core.shows.sections.anticipated.all.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.core.shows.sections.anticipated.all.AllShowsAnticipatedScreen

@Serializable
internal data object ShowsAnticipatedDestination

internal fun NavGraphBuilder.showsAnticipatedScreen(onNavigateBack: () -> Unit) {
    composable<ShowsAnticipatedDestination> {
        AllShowsAnticipatedScreen(
            viewModel = koinViewModel(),
            onNavigateBack = onNavigateBack,
        )
    }
}

internal fun NavController.navigateToAnticipatedShows() {
    navigate(route = ShowsAnticipatedDestination)
}
