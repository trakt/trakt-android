package tv.trakt.trakt.core.summary.history.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.summary.history.MediaHistoryScreen
import tv.trakt.trakt.core.summary.history.model.MediaHistoryType

@Serializable
internal data class MediaHistoryDestination(
    val mediaType: MediaHistoryType,
    val mediaId: Int,
    val mediaTitle: String,
    val watched: Int,
    val backgroundUrl: String? = null,
)

internal fun NavGraphBuilder.mediaHistoryScreen(
    onNavigateToEpisode: (showId: TraktId, episode: Episode) -> Unit,
    onNavigateBack: () -> Unit,
) {
    composable<MediaHistoryDestination> {
        MediaHistoryScreen(
            viewModel = koinViewModel(),
            onNavigateBack = onNavigateBack,
            onNavigateToEpisode = onNavigateToEpisode,
        )
    }
}

internal fun NavController.navigateToMediaHistory(
    mediaType: MediaHistoryType,
    mediaId: TraktId,
    mediaTitle: String,
    watched: Int,
    backgroundUrl: String? = null,
) {
    navigate(
        route = MediaHistoryDestination(
            mediaType = mediaType,
            mediaId = mediaId.value,
            mediaTitle = mediaTitle,
            watched = watched,
            backgroundUrl = backgroundUrl,
        ),
    )
}
