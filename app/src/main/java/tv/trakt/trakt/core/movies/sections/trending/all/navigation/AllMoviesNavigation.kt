package tv.trakt.trakt.core.movies.sections.trending.all.navigation

import android.content.Context
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import tv.trakt.trakt.MainActivity
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.movies.sections.trending.all.AllMoviesTrendingScreen

@Serializable
internal data object MoviesTrendingDestination

internal fun NavGraphBuilder.moviesTrendingScreen(
    context: Context?,
    onNavigateBack: () -> Unit,
    onNavigateToMovie: (TraktId) -> Unit,
) {
    val halloween = (context as? MainActivity)?.halloweenConfig?.enabled == true
    composable<MoviesTrendingDestination> {
        AllMoviesTrendingScreen(
            viewModel = koinViewModel {
                parametersOf(halloween)
            },
            onNavigateBack = onNavigateBack,
            onMovieClick = onNavigateToMovie,
        )
    }
}

internal fun NavController.navigateToTrendingMovies() {
    navigate(route = MoviesTrendingDestination)
}
