package tv.trakt.trakt.app.core.home.sections.recommended.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.app.core.home.sections.recommended.viewall.RecommendedViewAllScreen
import tv.trakt.trakt.common.model.TraktId

@Serializable
internal data object HomeRecommendedDestination

internal fun NavGraphBuilder.homeRecommendedScreen(
    onNavigateToShow: (TraktId) -> Unit,
    onNavigateToMovie: (TraktId) -> Unit,
) {
    composable<HomeRecommendedDestination> {
        RecommendedViewAllScreen(
            viewModel = koinViewModel(),
            onNavigateToShow = onNavigateToShow,
            onNavigateToMovie = onNavigateToMovie,
        )
    }
}

internal fun NavController.navigateToHomeRecommended() {
    navigate(route = HomeRecommendedDestination)
}
