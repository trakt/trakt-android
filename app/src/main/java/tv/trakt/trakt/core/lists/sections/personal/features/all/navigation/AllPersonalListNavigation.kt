package tv.trakt.trakt.core.lists.sections.personal.features.all.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.lists.sections.personal.features.all.AllPersonalListScreen

@Serializable
internal data class ListsPersonalDestination(
    val listId: Int,
    val listTitle: String,
    val listDescription: String?,
)

internal fun NavGraphBuilder.allPersonalListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToShow: (TraktId) -> Unit,
    onNavigateToMovie: (TraktId) -> Unit,
    onNavigateToEpisode: (TraktId, Episode) -> Unit,
) {
    composable<ListsPersonalDestination> {
        AllPersonalListScreen(
            viewModel = koinViewModel(),
            onNavigateBack = onNavigateBack,
            onShowClick = onNavigateToShow,
            onMovieClick = onNavigateToMovie,
            onEpisodeClick = onNavigateToEpisode,
        )
    }
}

internal fun NavController.navigateToPersonalList(
    listId: Int,
    listTitle: String,
    listDescription: String?,
) {
    navigate(
        route = ListsPersonalDestination(
            listId = listId,
            listTitle = listTitle,
            listDescription = listDescription,
        ),
    )
}
