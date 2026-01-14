package tv.trakt.trakt.core.summary.episodes.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Person
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.comments.model.CommentsFilter
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
    onEpisodeCLick: ((TraktId, Episode) -> Unit),
    onCommentsClick: ((Show, Episode, CommentsFilter) -> Unit),
    onPersonClick: ((Show, Episode, Person) -> Unit),
    onNavigateVip: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    composable<EpisodeDetailsDestination> {
        EpisodeDetailsScreen(
            viewModel = koinViewModel(),
            onShowClick = onShowClick,
            onEpisodeClick = onEpisodeCLick,
            onCommentsClick = onCommentsClick,
            onPersonClick = onPersonClick,
            onNavigateVip = onNavigateVip,
            onNavigateBack = onNavigateBack,
        )
    }
}

internal fun NavController.navigateToEpisode(
    showId: TraktId,
    episode: Episode,
) {
    navigateToEpisode(
        showId = showId,
        episodeId = episode.ids.trakt,
        episodeSeason = episode.season,
        episodeNumber = episode.number,
    )
}

internal fun NavController.navigateToEpisode(
    showId: TraktId,
    episodeId: TraktId,
    episodeSeason: Int,
    episodeNumber: Int,
) {
    navigate(
        route = EpisodeDetailsDestination(
            showId = showId.value,
            episodeId = episodeId.value,
            season = episodeSeason,
            episode = episodeNumber,
        ),
    )
}
