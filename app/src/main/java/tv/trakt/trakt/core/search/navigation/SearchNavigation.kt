package tv.trakt.trakt.core.search.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.common.helpers.extensions.popUpToTop
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.search.SearchScreen

@Serializable
internal data object SearchDestination

internal fun NavGraphBuilder.searchScreen(
    onNavigateToShow: (showId: TraktId) -> Unit,
    onNavigateToMovie: (movieId: TraktId) -> Unit,
    onNavigateToProfile: () -> Unit,
) {
    composable<SearchDestination> {
        SearchScreen(
            viewModel = koinViewModel(),
            onShowClick = onNavigateToShow,
            onMovieClick = onNavigateToMovie,
            onProfileClick = onNavigateToProfile,
        )
    }
}

internal fun NavController.navigateToSearch() {
    navigate(route = SearchDestination) {
        popUpToTop(this@navigateToSearch)
    }
}
