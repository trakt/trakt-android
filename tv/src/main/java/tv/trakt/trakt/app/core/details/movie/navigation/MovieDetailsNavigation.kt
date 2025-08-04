package tv.trakt.trakt.app.core.details.movie.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.app.common.model.CustomList
import tv.trakt.trakt.app.core.details.movie.MovieDetailsScreen
import tv.trakt.trakt.app.core.people.navigation.PersonDestination
import tv.trakt.trakt.common.model.TraktId

@Serializable
internal data class MovieDestination(
    val movieId: Int,
)

internal fun NavGraphBuilder.movieDetailsScreen(
    onNavigateToMovie: (TraktId) -> Unit,
    onNavigateToPerson: (PersonDestination) -> Unit,
    onNavigateToList: (CustomList) -> Unit,
    onNavigateToVideo: (String) -> Unit,
    onNavigateToStreamings: (showId: TraktId) -> Unit,
) {
    composable<MovieDestination> {
        MovieDetailsScreen(
            viewModel = koinViewModel(),
            onNavigateToMovie = onNavigateToMovie,
            onNavigateToPerson = onNavigateToPerson,
            onNavigateToList = onNavigateToList,
            onNavigateToVideo = onNavigateToVideo,
            onNavigateToStreamings = onNavigateToStreamings,
        )
    }
}

internal fun NavController.navigateToMovie(movieId: TraktId) {
    navigate(route = MovieDestination(movieId.value))
}
