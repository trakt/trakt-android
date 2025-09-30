package tv.trakt.trakt.core.movies.sections.trending.all.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.movies.sections.trending.all.AllMoviesTrendingScreen

@Serializable
internal data object MoviesTrendingDestination

internal fun NavGraphBuilder.moviesTrendingScreen(
    onNavigateBack: () -> Unit,
    onNavigateToMovie: (TraktId) -> Unit,
) {
    composable<MoviesTrendingDestination> {
        AllMoviesTrendingScreen(
            viewModel = koinViewModel(),
            onNavigateBack = onNavigateBack,
            onMovieClick = onNavigateToMovie,
        )
    }
}

internal fun NavController.navigateToTrendingMovies() {
    navigate(route = MoviesTrendingDestination)
}
