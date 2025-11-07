package tv.trakt.trakt.core.discover.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.common.helpers.extensions.popUpToTop
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.discover.DiscoverScreen

@Serializable
internal data object DiscoverDestination

internal fun NavGraphBuilder.discoverScreen(
    onNavigateToShow: (TraktId) -> Unit,
    onNavigateToMovie: (TraktId) -> Unit,
    onNavigateToAllTrending: () -> Unit = {},
    onNavigateToAllPopular: () -> Unit = {},
    onNavigateToAllAnticipated: () -> Unit = {},
    onNavigateToAllRecommended: () -> Unit = {},
) {
    composable<DiscoverDestination> {
        DiscoverScreen(
            viewModel = koinViewModel(),
            onNavigateToShow = onNavigateToShow,
            onNavigateToMovie = onNavigateToMovie,
            onNavigateToAllTrending = onNavigateToAllTrending,
            onNavigateToAllPopular = onNavigateToAllPopular,
            onNavigateToAllAnticipated = onNavigateToAllAnticipated,
            onNavigateToAllRecommended = onNavigateToAllRecommended,
        )
    }
}

internal fun NavController.navigateToDiscover() {
    navigate(route = DiscoverDestination) {
        popUpToTop(this@navigateToDiscover)
    }
}
