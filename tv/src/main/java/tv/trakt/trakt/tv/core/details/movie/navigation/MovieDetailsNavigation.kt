package tv.trakt.trakt.tv.core.details.movie.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.tv.common.model.CustomList
import tv.trakt.trakt.tv.core.details.movie.MovieDetailsScreen
import tv.trakt.trakt.tv.core.people.navigation.PersonDestination

@Serializable
internal data class MovieDestination(
    val movieId: Int,
)

internal fun NavGraphBuilder.movieDetailsScreen(
    onNavigateToMovie: (TraktId) -> Unit,
    onNavigateToPerson: (PersonDestination) -> Unit,
    onNavigateToList: (CustomList) -> Unit,
) {
    composable<MovieDestination> {
        MovieDetailsScreen(
            viewModel = koinViewModel(),
            onNavigateToMovie = onNavigateToMovie,
            onNavigateToPerson = onNavigateToPerson,
            onNavigateToList = onNavigateToList,
        )
    }
}

internal fun NavController.navigateToMovie(movieId: TraktId) {
    navigate(route = MovieDestination(movieId.value))
}
