package tv.trakt.trakt.core.movies.sections.trending.all.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.core.movies.sections.trending.all.AllMoviesScreen

@Serializable
internal data object MoviesTrendingDestination

internal fun NavGraphBuilder.moviesTrendingScreen(onNavigateBack: () -> Unit) {
    composable<MoviesTrendingDestination> {
        AllMoviesScreen(
            viewModel = koinViewModel(),
            onNavigateBack = onNavigateBack,
        )
    }
}

internal fun NavController.navigateToTrendingMovies() {
    navigate(route = MoviesTrendingDestination)
}
