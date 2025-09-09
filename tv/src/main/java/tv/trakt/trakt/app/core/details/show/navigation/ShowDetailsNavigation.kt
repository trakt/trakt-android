package tv.trakt.trakt.app.core.details.show.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.app.core.details.show.ShowDetailsScreen
import tv.trakt.trakt.app.core.episodes.model.Episode
import tv.trakt.trakt.app.core.people.navigation.PersonDestination
import tv.trakt.trakt.common.model.CustomList
import tv.trakt.trakt.common.model.TraktId

@Serializable
internal data class ShowDestination(
    val showId: Int,
)

internal fun NavGraphBuilder.showDetailsScreen(
    onNavigateToShow: (TraktId) -> Unit,
    onNavigateToEpisode: (showId: TraktId, episode: Episode) -> Unit,
    onNavigateToPerson: (PersonDestination) -> Unit,
    onNavigateToList: (CustomList) -> Unit,
    onNavigateToVideo: (String) -> Unit,
    onNavigateToStreamings: (showId: TraktId) -> Unit,
) {
    composable<ShowDestination> {
        ShowDetailsScreen(
            viewModel = koinViewModel(),
            onNavigateToShow = onNavigateToShow,
            onNavigateToEpisode = onNavigateToEpisode,
            onNavigateToPerson = onNavigateToPerson,
            onNavigateToList = onNavigateToList,
            onNavigateToVideo = onNavigateToVideo,
            onNavigateToStreamings = onNavigateToStreamings,
        )
    }
}

internal fun NavController.navigateToShow(showId: TraktId) {
    navigate(route = ShowDestination(showId.value))
}
