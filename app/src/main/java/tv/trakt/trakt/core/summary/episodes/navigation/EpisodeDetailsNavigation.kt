package tv.trakt.trakt.core.summary.episodes.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.summary.episodes.EpisodeDetailsScreen

@Serializable
internal data class EpisodeDetailsDestination(
    val showId: Int,
    val episodeId: Int,
    val season: Int,
    val episode: Int,
)

internal fun NavGraphBuilder.episodeDetailsScreen(
    onShowClick: ((Show) -> Unit),
    onCommentsClick: ((Show, Episode) -> Unit),
    onNavigateBack: () -> Unit,
) {
    composable<EpisodeDetailsDestination> {
        EpisodeDetailsScreen(
            viewModel = koinViewModel(),
            onShowClick = onShowClick,
            onCommentsClick = onCommentsClick,
            onNavigateBack = onNavigateBack,
        )
    }
}

internal fun NavController.navigateToEpisode(
    showId: TraktId,
    episode: Episode,
) {
    navigate(
        route = EpisodeDetailsDestination(
            showId = showId.value,
            episodeId = episode.ids.trakt.value,
            season = episode.season,
            episode = episode.number,
        ),
    )
}
