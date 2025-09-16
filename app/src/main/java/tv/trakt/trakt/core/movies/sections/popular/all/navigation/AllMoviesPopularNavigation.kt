package tv.trakt.trakt.core.movies.sections.popular.all.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.core.movies.sections.popular.all.AllMoviesPopularScreen

@Serializable
internal data object MoviesPopularDestination

internal fun NavGraphBuilder.moviesPopularScreen(onNavigateBack: () -> Unit) {
    composable<MoviesPopularDestination> {
        AllMoviesPopularScreen(
            viewModel = koinViewModel(),
            onNavigateBack = onNavigateBack,
        )
    }
}

internal fun NavController.navigateToPopularMovies() {
    navigate(route = MoviesPopularDestination)
}