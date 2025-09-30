package tv.trakt.trakt.core.movies.sections.popular.all.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.movies.sections.popular.all.AllMoviesPopularScreen

@Serializable
internal data object MoviesPopularDestination

internal fun NavGraphBuilder.moviesPopularScreen(
    onNavigateBack: () -> Unit,
    onNavigateToMovie: (TraktId) -> Unit,
) {
    composable<MoviesPopularDestination> {
        AllMoviesPopularScreen(
            viewModel = koinViewModel(),
            onNavigateBack = onNavigateBack,
            onMovieClick = onNavigateToMovie,
        )
    }
}

internal fun NavController.navigateToPopularMovies() {
    navigate(route = MoviesPopularDestination)
}
