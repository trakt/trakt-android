package tv.trakt.trakt.core.comments.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.comments.CommentsScreen

@Serializable
internal data class CommentsDestination(
    val mediaId: Int,
    val mediaType: String,
    val mediaImage: String?,
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
) {
    navigate(
        route = CommentsDestination(
            mediaId = mediaId.value,
            mediaType = mediaType.name,
            mediaImage = mediaImage,
        ),
    )
}
