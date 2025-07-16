package tv.trakt.app.tv.core.details.lists.details.movies.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.app.tv.common.model.CustomList
import tv.trakt.app.tv.common.model.TraktId
import tv.trakt.app.tv.core.details.lists.details.movies.CustomListMoviesScreen

@Serializable
internal data class CustomListMoviesDestination(
    val listId: Int,
    val listName: String,
)

internal fun NavGraphBuilder.customListMovies(onNavigateToMovie: (TraktId) -> Unit) {
    composable<CustomListMoviesDestination> {
        CustomListMoviesScreen(
            viewModel = koinViewModel(),
            onNavigateToMovie = onNavigateToMovie,
        )
    }
}

internal fun NavController.navigateToCustomListMovies(list: CustomList) {
    navigate(
        route = CustomListMoviesDestination(
            listId = list.ids.trakt.value,
            listName = list.name,
        ),
    )
}
