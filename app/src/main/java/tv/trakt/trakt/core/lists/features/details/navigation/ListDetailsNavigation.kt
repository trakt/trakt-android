package tv.trakt.trakt.core.lists.features.details.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.MediaType.Movie
import tv.trakt.trakt.common.model.MediaType.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.lists.CustomList
import tv.trakt.trakt.core.lists.features.details.ListDetailsScreen

@Serializable
internal data class ListsDetailsDestination(
    val listJson: String,
    val mediaId: Int,
    val mediaType: List<String>,
    val mediaImage: String?,
) {
    init {
        require(mediaType.size in 1..2) {
            "mediaType must contain either 1 or 2 types"
        }

        require(mediaType.all { it == Show.name || it == Movie.name }) {
            "mediaType must be either SHOW or MOVIE"
        }
    }
}

internal fun NavGraphBuilder.listDetailsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToShow: (TraktId) -> Unit,
    onNavigateToMovie: (TraktId) -> Unit,
    onNavigateToEpisode: (TraktId, Episode) -> Unit,
) {
    composable<ListsDetailsDestination> {
        ListDetailsScreen(
            viewModel = koinViewModel(),
            onNavigateBack = onNavigateBack,
            onShowClick = onNavigateToShow,
            onMovieClick = onNavigateToMovie,
            onEpisodeClick = onNavigateToEpisode,
        )
    }
}

internal fun NavController.navigateToListDetails(
    list: CustomList,
    mediaId: TraktId,
    mediaType: List<MediaType>,
    mediaImage: String?,
) {
    navigate(
        route = ListsDetailsDestination(
            listJson = Json.encodeToString(list),
            mediaId = mediaId.value,
            mediaType = mediaType.map { it.name },
            mediaImage = mediaImage,
        ),
    )
}
