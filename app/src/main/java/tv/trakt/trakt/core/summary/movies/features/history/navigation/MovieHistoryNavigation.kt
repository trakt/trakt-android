package tv.trakt.trakt.core.summary.movies.features.history.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.summary.movies.features.history.MovieHistoryScreen

@Serializable
internal data class MovieHistoryDestination(
    val movieId: Int,
    val movieTitle: String,
    val watched: Int,
    val backgroundUrl: String? = null,
)

internal fun NavGraphBuilder.movieHistoryScreen(onNavigateBack: () -> Unit) {
    composable<MovieHistoryDestination> {
        MovieHistoryScreen(
            viewModel = koinViewModel(),
            onNavigateBack = onNavigateBack,
        )
    }
}

internal fun NavController.navigateToMovieHistory(
    movieId: TraktId,
    movieTitle: String,
    watched: Int,
    backgroundUrl: String? = null,
) {
    navigate(
        route = MovieHistoryDestination(
            movieId = movieId.value,
            movieTitle = movieTitle,
            watched = watched,
            backgroundUrl = backgroundUrl,
        ),
    )
}
