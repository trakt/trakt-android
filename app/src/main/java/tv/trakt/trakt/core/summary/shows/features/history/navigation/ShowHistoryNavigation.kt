package tv.trakt.trakt.core.summary.shows.features.history.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.summary.shows.features.history.ShowHistoryScreen

@Serializable
internal data class ShowHistoryDestination(
    val showId: Int,
    val showTitle: String,
    val watched: Int,
    val backgroundUrl: String? = null,
)

internal fun NavGraphBuilder.showHistoryScreen(
    onNavigateToEpisode: (showId: TraktId, episode: Episode) -> Unit,
    onNavigateBack: () -> Unit,
) {
    composable<ShowHistoryDestination> {
        ShowHistoryScreen(
            viewModel = koinViewModel(),
            onNavigateBack = onNavigateBack,
            onNavigateToEpisode = onNavigateToEpisode,
        )
    }
}

internal fun NavController.navigateToShowHistory(
    showId: TraktId,
    showTitle: String,
    watched: Int,
    backgroundUrl: String? = null,
) {
    navigate(
        route = ShowHistoryDestination(
            showId = showId.value,
            showTitle = showTitle,
            watched = watched,
            backgroundUrl = backgroundUrl,
        ),
    )
}
