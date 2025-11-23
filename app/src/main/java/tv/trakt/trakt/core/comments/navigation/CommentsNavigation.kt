package tv.trakt.trakt.core.comments.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.SeasonEpisode
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.comments.CommentsScreen
import tv.trakt.trakt.core.comments.model.CommentsFilter

@Serializable
internal data class CommentsDestination(
    val mediaId: Int,
    val mediaType: MediaType,
    val mediaImage: String?,
    val mediaShowId: Int? = null,
    val mediaEpisode: Int? = null,
    val mediaSeason: Int? = null,
    val initialFilter: CommentsFilter,
)

internal fun NavGraphBuilder.commentsScreen(onNavigateBack: () -> Unit) {
    composable<CommentsDestination> {
        CommentsScreen(
            viewModel = koinViewModel(),
            onNavigateBack = onNavigateBack,
        )
    }
}

internal fun NavController.navigateToComments(
    mediaId: TraktId,
    mediaType: MediaType,
    mediaImage: String?,
    filter: CommentsFilter,
) {
    navigate(
        route = CommentsDestination(
            mediaId = mediaId.value,
            mediaType = mediaType,
            mediaImage = mediaImage,
            initialFilter = filter,
        ),
    )
}

internal fun NavController.navigateToComments(
    episodeId: TraktId,
    showId: TraktId,
    showImage: String?,
    seasonEpisode: SeasonEpisode,
    filter: CommentsFilter,
) {
    navigate(
        route = CommentsDestination(
            mediaType = MediaType.EPISODE,
            mediaId = episodeId.value,
            mediaShowId = showId.value,
            mediaEpisode = seasonEpisode.episode,
            mediaSeason = seasonEpisode.season,
            mediaImage = showImage,
            initialFilter = filter,
        ),
    )
}
