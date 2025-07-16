package tv.trakt.app.tv.core.details.show.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.app.tv.common.model.CustomList
import tv.trakt.app.tv.common.model.TraktId
import tv.trakt.app.tv.core.details.show.ShowDetailsScreen
import tv.trakt.app.tv.core.episodes.model.Episode
import tv.trakt.app.tv.core.people.navigation.PersonDestination

@Serializable
internal data class ShowDestination(
    val showId: Int,
)

internal fun NavGraphBuilder.showDetailsScreen(
    onNavigateToShow: (TraktId) -> Unit,
    onNavigateToEpisode: (showId: TraktId, episode: Episode) -> Unit,
    onNavigateToPerson: (PersonDestination) -> Unit,
    onNavigateToList: (CustomList) -> Unit,
) {
    composable<ShowDestination> {
        ShowDetailsScreen(
            viewModel = koinViewModel(),
            onNavigateToShow = onNavigateToShow,
            onNavigateToEpisode = onNavigateToEpisode,
            onNavigateToPerson = onNavigateToPerson,
            onNavigateToList = onNavigateToList,
        )
    }
}

internal fun NavController.navigateToShow(showId: TraktId) {
    navigate(route = ShowDestination(showId.value))
}
