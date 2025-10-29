package tv.trakt.trakt.core.movies.sections.recommended.all.navigation

import android.content.Context
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import tv.trakt.trakt.MainActivity
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.movies.sections.recommended.all.AllMoviesRecommendedScreen

@Serializable
internal data object MoviesRecommendedDestination

internal fun NavGraphBuilder.moviesRecommendedScreen(
    context: Context?,
    onNavigateBack: () -> Unit,
    onNavigateToMovie: (TraktId) -> Unit,
) {
    val halloween = (context as? MainActivity)?.halloweenConfig?.enabled == true
    composable<MoviesRecommendedDestination> {
        AllMoviesRecommendedScreen(
            viewModel = koinViewModel {
                parametersOf(halloween)
            },
            onNavigateBack = onNavigateBack,
            onMovieClick = onNavigateToMovie,
        )
    }
}

internal fun NavController.navigateToRecommendedMovies() {
    navigate(route = MoviesRecommendedDestination)
}
