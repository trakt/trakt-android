package tv.trakt.trakt.core.movies.sections.recommended.all.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.movies.sections.recommended.all.AllMoviesRecommendedScreen

@Serializable
internal data object MoviesRecommendedDestination

internal fun NavGraphBuilder.moviesRecommendedScreen(
    onNavigateBack: () -> Unit,
    onNavigateToMovie: (TraktId) -> Unit,
) {
    composable<MoviesRecommendedDestination> {
        AllMoviesRecommendedScreen(
            viewModel = koinViewModel(),
            onNavigateBack = onNavigateBack,
            onMovieClick = onNavigateToMovie,
        )
    }
}

internal fun NavController.navigateToRecommendedMovies() {
    navigate(route = MoviesRecommendedDestination)
}
