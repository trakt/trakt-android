package tv.trakt.trakt.core.movies.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import tv.trakt.trakt.common.helpers.extensions.popUpToTop
import tv.trakt.trakt.core.movies.MoviesScreen
import tv.trakt.trakt.tv.common.model.TraktId

@Serializable
internal data object MoviesDestination

internal fun NavGraphBuilder.moviesScreen(onNavigateToMovie: (TraktId) -> Unit) {
    composable<MoviesDestination> {
        MoviesScreen(
            onNavigateToMovie = onNavigateToMovie,
        )
    }
}

internal fun NavController.navigateToMovies() {
    navigate(route = MoviesDestination) {
        popUpToTop(this@navigateToMovies)
    }
}
