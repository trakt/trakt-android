package tv.trakt.trakt.core.summary.shows.features.seasons.all.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Person
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.summary.shows.features.seasons.all.AllShowSeasonsScreen

@Serializable
internal data class AllShowSeasonsDestination(
    val showId: Int,
    val initialSeason: Int? = null,
    val backgroundUrl: String? = null,
)

internal fun NavGraphBuilder.allShowSeasonsScreen(
    onNavigateToEpisode: (showId: TraktId, episode: Episode) -> Unit,
    onNavigateToPerson: (show: Show, person: Person) -> Unit,
    onNavigateBack: () -> Unit,
) {
    composable<AllShowSeasonsDestination> {
        AllShowSeasonsScreen(
            viewModel = koinViewModel(),
            onEpisodeClick = onNavigateToEpisode,
            onPersonClick = onNavigateToPerson,
            onNavigateBack = onNavigateBack,
        )
    }
}

internal fun NavController.navigateToAllShowSeasons(
    showId: TraktId,
    initialSeason: Int? = null,
    backgroundUrl: String? = null,
) {
    navigate(
        route = AllShowSeasonsDestination(
            showId = showId.value,
            initialSeason = initialSeason,
            backgroundUrl = backgroundUrl,
        ),
    )
}
