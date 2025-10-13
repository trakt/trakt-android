package tv.trakt.trakt.core.lists.features.details.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.lists.features.details.ListDetailsScreen

@Serializable
internal data class ListsDetailsDestination(
    val listId: Int,
    val listTitle: String,
    val listDescription: String?,
    val mediaId: Int,
    val mediaImage: String?,
)

internal fun NavGraphBuilder.listDetailsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToMovie: (TraktId) -> Unit,
) {
    composable<ListsDetailsDestination> {
        ListDetailsScreen(
            viewModel = koinViewModel(),
            onNavigateBack = onNavigateBack,
            onMovieClick = onNavigateToMovie,
        )
    }
}

internal fun NavController.navigateToListDetails(
    listId: Int,
    listTitle: String,
    listDescription: String?,
    mediaId: TraktId,
    mediaImage: String?,
) {
    navigate(
        route = ListsDetailsDestination(
            listId = listId,
            listTitle = listTitle,
            listDescription = listDescription,
            mediaId = mediaId.value,
            mediaImage = mediaImage,
        ),
    )
}
