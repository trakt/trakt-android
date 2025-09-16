package tv.trakt.trakt.core.movies.sections.anticipated.all.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.core.movies.sections.anticipated.all.AllMoviesAnticipatedScreen

@Serializable
internal data object MoviesAnticipatedDestination

internal fun NavGraphBuilder.moviesAnticipatedScreen(onNavigateBack: () -> Unit) {
    composable<MoviesAnticipatedDestination> {
        AllMoviesAnticipatedScreen(
            viewModel = koinViewModel(),
            onNavigateBack = onNavigateBack,
        )
    }
}

internal fun NavController.navigateToAnticipatedMovies() {
    navigate(route = MoviesAnticipatedDestination)
}