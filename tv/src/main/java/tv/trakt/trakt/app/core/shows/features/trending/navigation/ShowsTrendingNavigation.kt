package tv.trakt.trakt.app.core.shows.features.trending.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.app.core.shows.features.trending.ShowsTrendingScreen
import tv.trakt.trakt.common.model.TraktId

@Serializable
internal data object ShowsTrendingDestination

internal fun NavGraphBuilder.showsTrendingScreen(onNavigateToShow: (TraktId) -> Unit) {
    composable<ShowsTrendingDestination> {
        ShowsTrendingScreen(
            viewModel = koinViewModel(),
            onNavigateToShow = onNavigateToShow,
        )
    }
}

internal fun NavController.navigateToShowsTrending() {
    navigate(route = ShowsTrendingDestination)
}
