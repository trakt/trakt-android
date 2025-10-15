package tv.trakt.trakt.core.summary.episodes.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import tv.trakt.trakt.common.model.CustomList
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId

@Serializable
internal data class EpisodeDetailsDestination(
    val showId: Int,
    val episodeId: Int,
    val season: Int,
    val episode: Int,
)

internal fun NavGraphBuilder.episodeDetailsScreen(
    onNavigateToShow: (TraktId) -> Unit,
    onNavigateToComments: (Show) -> Unit,
    onNavigateToList: (Show, CustomList) -> Unit,
    onNavigateBack: () -> Unit,
) {
    composable<EpisodeDetailsDestination> {
//        ShowDetailsScreen(
//            viewModel = koinViewModel(),
//            onShowClick = { onNavigateToShow(it.ids.trakt) },
//            onCommentsClick = { onNavigateToComments(it) },
//            onListClick = { show, list -> onNavigateToList(show, list) },
//            onNavigateBack = onNavigateBack,
//        )
    }
}

internal fun NavController.navigateToEpisode(
    showId: TraktId,
    episodeId: TraktId,
    season: Int,
    episode: Int,
) {
    navigate(
        route = EpisodeDetailsDestination(
            showId = showId.value,
            episodeId = episodeId.value,
            season = season,
            episode = episode,
        ),
    )
}
