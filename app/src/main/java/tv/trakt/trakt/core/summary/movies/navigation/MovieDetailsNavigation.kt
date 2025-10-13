package tv.trakt.trakt.core.summary.movies.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.common.model.CustomList
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.summary.movies.MovieDetailsScreen

@Serializable
internal data class MovieDetailsDestination(
    val movieId: Int,
)

internal fun NavGraphBuilder.movieDetailsScreen(
    onNavigateToMovie: (TraktId) -> Unit,
    onNavigateToComments: (Movie) -> Unit,
    onNavigateToList: (Movie, CustomList) -> Unit,
    onNavigateBack: () -> Unit,
) {
    composable<MovieDetailsDestination> {
        MovieDetailsScreen(
            viewModel = koinViewModel(),
            onMovieClick = { onNavigateToMovie(it.ids.trakt) },
            onCommentsClick = { onNavigateToComments(it) },
            onListClick = { movie, list -> onNavigateToList(movie, list) },
            onNavigateBack = onNavigateBack,
        )
    }
}

internal fun NavController.navigateToMovie(movieId: TraktId) {
    navigate(route = MovieDetailsDestination(movieId.value))
}
