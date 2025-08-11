package tv.trakt.trakt.app.core.search.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.app.common.model.TraktId
import tv.trakt.trakt.app.core.search.SearchScreen
import tv.trakt.trakt.common.helpers.extensions.popUpToTop

@Serializable
internal data object SearchDestination

internal fun NavGraphBuilder.searchScreen(
    onNavigateToShow: (showId: TraktId) -> Unit,
    onNavigateToMovie: (movieId: TraktId) -> Unit,
) {
    composable<SearchDestination> {
        SearchScreen(
            viewModel = koinViewModel(),
            onShowClick = onNavigateToShow,
            onMovieClick = onNavigateToMovie,
        )
    }
}

internal fun NavController.navigateToSearch() {
    navigate(route = SearchDestination) {
        popUpToTop(this@navigateToSearch)
    }
}
