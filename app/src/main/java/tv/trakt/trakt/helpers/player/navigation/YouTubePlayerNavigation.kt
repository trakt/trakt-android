package tv.trakt.trakt.helpers.player.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.helpers.player.YouTubePlayerScreen

@Serializable
internal data class YouTubePlayerDestination(
    val videoUrl: String,
)

internal fun NavController.navigateToYouTubePlayer(videoUrl: String) {
    navigate(
        route = YouTubePlayerDestination(
            videoUrl = videoUrl,
        ),
    )
}

internal fun NavGraphBuilder.youTubePlayerScreen(onNavigateBack: () -> Unit) {
    composable<YouTubePlayerDestination> {
        YouTubePlayerScreen(
            viewModel = koinViewModel(),
            onNavigateBack = onNavigateBack,
        )
    }
}
