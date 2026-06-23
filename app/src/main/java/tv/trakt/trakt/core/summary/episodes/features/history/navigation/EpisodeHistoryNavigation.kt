package tv.trakt.trakt.core.summary.episodes.features.history.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.summary.episodes.features.history.EpisodeHistoryScreen

@Serializable
internal data class EpisodeHistoryDestination(
    val episodeId: Int,
    val episodeTitle: String,
    val watched: Int,
    val backgroundUrl: String? = null,
)

internal fun NavGraphBuilder.episodeHistoryScreen(onNavigateBack: () -> Unit) {
    composable<EpisodeHistoryDestination> {
        EpisodeHistoryScreen(
            viewModel = koinViewModel(),
            onNavigateBack = onNavigateBack,
        )
    }
}

internal fun NavController.navigateToEpisodeHistory(
    episodeId: TraktId,
    episodeTitle: String,
    watched: Int,
    backgroundUrl: String? = null,
) {
    navigate(
        route = EpisodeHistoryDestination(
            episodeId = episodeId.value,
            episodeTitle = episodeTitle,
            watched = watched,
            backgroundUrl = backgroundUrl,
        ),
    )
}
