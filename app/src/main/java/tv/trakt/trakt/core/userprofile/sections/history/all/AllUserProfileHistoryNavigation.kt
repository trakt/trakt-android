package tv.trakt.trakt.core.userprofile.sections.history.all

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.TraktId

@Serializable
internal data class AllUserProfileHistoryDestination(
    val userId: Int,
)

internal fun NavGraphBuilder.allUserProfileHistoryScreen(
    onNavigateToShow: (TraktId) -> Unit,
    onNavigateToEpisode: (showId: TraktId, episode: Episode) -> Unit,
    onNavigateToMovie: (TraktId) -> Unit,
    onNavigateBack: () -> Unit,
) {
    composable<AllUserProfileHistoryDestination> {
        AllUserProfileHistoryScreen(
            onNavigateToShow = onNavigateToShow,
            onNavigateToEpisode = onNavigateToEpisode,
            onNavigateToMovie = onNavigateToMovie,
            onNavigateBack = onNavigateBack,
        )
    }
}

internal fun NavController.navigateToAllUserProfileHistory(userId: TraktId) {
    navigate(route = AllUserProfileHistoryDestination(userId = userId.value))
}
