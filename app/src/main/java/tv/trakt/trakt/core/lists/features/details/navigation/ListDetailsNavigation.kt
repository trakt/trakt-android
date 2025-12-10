package tv.trakt.trakt.core.lists.features.details.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.MediaType.MOVIE
import tv.trakt.trakt.common.model.MediaType.SHOW
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.lists.features.details.ListDetailsScreen

@Serializable
internal data class ListsDetailsDestination(
    val listId: Int,
    val listTitle: String,
    val listDescription: String?,
    val mediaId: Int,
    val mediaType: String,
    val mediaImage: String?,
) {
    init {
        require(mediaType in arrayOf(SHOW.name, MOVIE.name)) {
            "Invalid media type: $mediaType"
        }
    }
}

internal fun NavGraphBuilder.listDetailsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToShow: (TraktId) -> Unit,
    onNavigateToMovie: (TraktId) -> Unit,
) {
    composable<ListsDetailsDestination> {
        ListDetailsScreen(
            viewModel = koinViewModel(),
            onNavigateBack = onNavigateBack,
            onShowClick = onNavigateToShow,
            onMovieClick = onNavigateToMovie,
        )
    }
}

internal fun NavController.navigateToListDetails(
    listId: Int,
    listTitle: String,
    listDescription: String?,
    mediaId: TraktId,
    mediaType: MediaType,
    mediaImage: String?,
) {
    navigate(
        route = ListsDetailsDestination(
            listId = listId,
            listTitle = listTitle,
            listDescription = listDescription,
            mediaId = mediaId.value,
            mediaType = mediaType.name,
            mediaImage = mediaImage,
        ),
    )
}
