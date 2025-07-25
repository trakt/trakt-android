package tv.trakt.trakt.tv.core.lists.details.movies.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.tv.core.lists.details.movies.MoviesWatchlistScreen

@Serializable
internal data object WatchlistMoviesDestination

internal fun NavGraphBuilder.watchlistMovies(onNavigateToMovie: (TraktId) -> Unit) {
    composable<WatchlistMoviesDestination> {
        MoviesWatchlistScreen(
            viewModel = koinViewModel(),
            onNavigateToMovie = onNavigateToMovie,
        )
    }
}

internal fun NavController.navigateToWatchlistMovies() {
    navigate(
        route = WatchlistMoviesDestination,
    )
}
