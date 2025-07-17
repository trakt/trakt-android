package tv.trakt.trakt.tv.core.details.episode.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.tv.common.model.TraktId
import tv.trakt.trakt.tv.core.details.episode.EpisodeDetailsScreen
import tv.trakt.trakt.tv.core.episodes.model.Episode
import tv.trakt.trakt.tv.core.people.navigation.PersonDestination

@Serializable
internal data class EpisodeDestination(
    val showId: Int,
    val episodeId: Int,
    val season: Int,
    val episode: Int,
)

internal fun NavGraphBuilder.episodeDetailsScreen(
    onNavigateToShow: (TraktId) -> Unit,
    onNavigateToEpisode: (showId: TraktId, episode: Episode) -> Unit,
    onNavigateToPerson: (PersonDestination) -> Unit,
) {
    composable<EpisodeDestination> {
        EpisodeDetailsScreen(
            viewModel = koinViewModel(),
            onNavigateToShow = onNavigateToShow,
            onNavigateToEpisode = onNavigateToEpisode,
            onNavigateToPerson = onNavigateToPerson,
        )
    }
}

internal fun NavController.navigateToEpisode(
    showId: TraktId,
    episode: Episode,
) {
    navigate(
        route = EpisodeDestination(
            showId = showId.value,
            episodeId = episode.ids.trakt.value,
            season = episode.season,
            episode = episode.number,
        ),
    )
}
