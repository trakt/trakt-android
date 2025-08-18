package tv.trakt.trakt.app.core.movies.features.recommended.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.app.core.movies.features.recommended.MoviesRecommendedScreen
import tv.trakt.trakt.common.model.TraktId

@Serializable
internal data object MoviesRecommendedDestination

internal fun NavGraphBuilder.moviesRecommendedScreen(onNavigateToMovie: (TraktId) -> Unit) {
    composable<MoviesRecommendedDestination> {
        MoviesRecommendedScreen(
            viewModel = koinViewModel(),
            onNavigateToMovie = onNavigateToMovie,
        )
    }
}

internal fun NavController.navigateToMoviesRecommended() {
    navigate(route = MoviesRecommendedDestination)
}
