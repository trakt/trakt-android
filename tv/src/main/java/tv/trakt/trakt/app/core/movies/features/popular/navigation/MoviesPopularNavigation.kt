package tv.trakt.trakt.app.core.movies.features.popular.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.app.core.movies.features.popular.MoviesPopularScreen
import tv.trakt.trakt.common.model.TraktId

@Serializable
internal data object MoviesPopularDestination

internal fun NavGraphBuilder.moviesPopularScreen(onNavigateToMovie: (TraktId) -> Unit) {
    composable<MoviesPopularDestination> {
        MoviesPopularScreen(
            viewModel = koinViewModel(),
            onNavigateToMovie = onNavigateToMovie,
        )
    }
}

internal fun NavController.navigateToMoviesPopular() {
    navigate(route = MoviesPopularDestination)
}
