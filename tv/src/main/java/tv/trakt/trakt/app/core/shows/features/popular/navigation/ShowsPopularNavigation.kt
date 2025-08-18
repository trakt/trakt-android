package tv.trakt.trakt.app.core.shows.features.popular.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.app.core.shows.features.popular.ShowsPopularScreen
import tv.trakt.trakt.common.model.TraktId

@Serializable
internal data object ShowsPopularDestination

internal fun NavGraphBuilder.showsPopularScreen(onNavigateToShow: (TraktId) -> Unit) {
    composable<ShowsPopularDestination> {
        ShowsPopularScreen(
            viewModel = koinViewModel(),
            onNavigateToShow = onNavigateToShow,
        )
    }
}

internal fun NavController.navigateToShowsPopular() {
    navigate(route = ShowsPopularDestination)
}
