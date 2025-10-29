package tv.trakt.trakt.core.movies.sections.anticipated.all.navigation

import android.content.Context
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import tv.trakt.trakt.MainActivity
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.movies.sections.anticipated.all.AllMoviesAnticipatedScreen

@Serializable
internal data object MoviesAnticipatedDestination

internal fun NavGraphBuilder.moviesAnticipatedScreen(
    context: Context?,
    onNavigateBack: () -> Unit,
    onNavigateToMovie: (TraktId) -> Unit,
) {
    val halloween = (context as? MainActivity)?.halloweenConfig?.enabled == true
    composable<MoviesAnticipatedDestination> {
        AllMoviesAnticipatedScreen(
            viewModel = koinViewModel {
                parametersOf(halloween)
            },
            onNavigateBack = onNavigateBack,
            onMovieClick = onNavigateToMovie,
        )
    }
}

internal fun NavController.navigateToAnticipatedMovies() {
    navigate(route = MoviesAnticipatedDestination)
}
