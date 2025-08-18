package tv.trakt.trakt.app.core.shows.features.recommended.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.app.core.shows.features.recommended.ShowsRecommendedScreen
import tv.trakt.trakt.common.model.TraktId

@Serializable
internal data object ShowsRecommendedDestination

internal fun NavGraphBuilder.showsRecommendedScreen(onNavigateToShow: (TraktId) -> Unit) {
    composable<ShowsRecommendedDestination> {
        ShowsRecommendedScreen(
            viewModel = koinViewModel(),
            onNavigateToShow = onNavigateToShow,
        )
    }
}

internal fun NavController.navigateToShowsRecommended() {
    navigate(route = ShowsRecommendedDestination)
}
