package tv.trakt.trakt.core.shows.sections.popular.all.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.shows.sections.popular.all.AllShowsPopularScreen

@Serializable
internal data object ShowsPopularDestination

internal fun NavGraphBuilder.showsPopularScreen(
    onNavigateBack: () -> Unit,
    onNavigateToShow: (TraktId) -> Unit,
) {
    composable<ShowsPopularDestination> {
        AllShowsPopularScreen(
            viewModel = koinViewModel(),
            onNavigateToShow = onNavigateToShow,
            onNavigateBack = onNavigateBack,
        )
    }
}

internal fun NavController.navigateToPopularShows() {
    navigate(route = ShowsPopularDestination)
}
