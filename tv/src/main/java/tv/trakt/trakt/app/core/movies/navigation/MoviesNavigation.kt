package tv.trakt.trakt.app.core.movies.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.app.core.movies.MoviesScreen
import tv.trakt.trakt.common.helpers.extensions.popUpToTop
import tv.trakt.trakt.common.model.TraktId

@Serializable
internal data object MoviesDestination

internal fun NavGraphBuilder.moviesScreen(onNavigateToMovie: (TraktId) -> Unit) {
    composable<MoviesDestination> {
        MoviesScreen(
            viewModel = koinViewModel(),
            onNavigateToMovie = onNavigateToMovie,
        )
    }
}

internal fun NavController.navigateToMovies() {
    navigate(route = MoviesDestination) {
        popUpToTop(this@navigateToMovies)
    }
}
