package tv.trakt.trakt.core.search.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.common.helpers.extensions.popUpToTop
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.search.SearchScreen
import tv.trakt.trakt.core.search.model.SearchInput

@Serializable
internal data object SearchDestination

internal fun NavGraphBuilder.searchScreen(
    searchInput: SearchInput,
    onSearchLoading: (Boolean) -> Unit,
    onNavigateToShow: (showId: TraktId) -> Unit,
    onNavigateToMovie: (movieId: TraktId) -> Unit,
    onNavigateToPerson: (personId: TraktId) -> Unit,
) {
    composable<SearchDestination> {
        SearchScreen(
            viewModel = koinViewModel(),
            searchInput = searchInput,
            onSearchLoading = onSearchLoading,
            onShowClick = onNavigateToShow,
            onMovieClick = onNavigateToMovie,
            onPersonClick = onNavigateToPerson,
        )
    }
}

internal fun NavController.navigateToSearch() {
    navigate(route = SearchDestination) {
        popUpToTop(this@navigateToSearch)
    }
}
