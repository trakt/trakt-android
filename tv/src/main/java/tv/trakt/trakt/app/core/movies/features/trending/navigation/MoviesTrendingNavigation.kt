package tv.trakt.trakt.app.core.movies.features.trending.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.app.core.movies.features.trending.MoviesTrendingScreen
import tv.trakt.trakt.common.model.TraktId

@Serializable
internal data object MoviesTrendingDestination

internal fun NavGraphBuilder.moviesTrendingScreen(onNavigateToMovie: (TraktId) -> Unit) {
    composable<MoviesTrendingDestination> {
        MoviesTrendingScreen(
            viewModel = koinViewModel(),
            onNavigateToMovie = onNavigateToMovie,
        )
    }
}

internal fun NavController.navigateToMoviesTrending() {
    navigate(route = MoviesTrendingDestination)
}
