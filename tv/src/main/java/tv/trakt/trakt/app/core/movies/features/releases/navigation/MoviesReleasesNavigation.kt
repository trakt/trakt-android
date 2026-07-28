package tv.trakt.trakt.app.core.movies.features.releases.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.app.core.movies.features.releases.MoviesReleasesScreen
import tv.trakt.trakt.common.model.TraktId

@Serializable
internal data object MoviesReleasesDestination

internal fun NavGraphBuilder.moviesReleasesScreen(onNavigateToMovie: (TraktId) -> Unit) {
    composable<MoviesReleasesDestination> {
        MoviesReleasesScreen(
            viewModel = koinViewModel(),
            onNavigateToMovie = onNavigateToMovie,
        )
    }
}

internal fun NavController.navigateToMoviesReleases() {
    navigate(route = MoviesReleasesDestination)
}
