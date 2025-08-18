package tv.trakt.trakt.app.core.movies.features.anticipated.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.app.core.movies.features.anticipated.MoviesAnticipatedScreen
import tv.trakt.trakt.common.model.TraktId

@Serializable
internal data object MoviesAnticipatedDestination

internal fun NavGraphBuilder.moviesAnticipatedScreen(onNavigateToMovie: (TraktId) -> Unit) {
    composable<MoviesAnticipatedDestination> {
        MoviesAnticipatedScreen(
            viewModel = koinViewModel(),
            onNavigateToMovie = onNavigateToMovie,
        )
    }
}

internal fun NavController.navigateToMoviesAnticipated() {
    navigate(route = MoviesAnticipatedDestination)
}
