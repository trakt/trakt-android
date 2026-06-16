package tv.trakt.trakt.core.profile.sections.activity.all.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.profile.sections.activity.all.ProfileAllActivityScreen

@Serializable
internal data class ProfileAllActivityDestination(
    val unused: Boolean = false,
)

internal fun NavGraphBuilder.profileAllActivityScreen(
    onNavigateToShow: (TraktId) -> Unit,
    onNavigateToMovie: (TraktId) -> Unit,
    onNavigateToEpisode: (showId: TraktId, episode: Episode) -> Unit,
    onNavigateBack: () -> Unit,
) {
    composable<ProfileAllActivityDestination> {
        ProfileAllActivityScreen(
            viewModel = koinViewModel(),
            onShowClick = onNavigateToShow,
            onMovieClick = onNavigateToMovie,
            onEpisodeClick = onNavigateToEpisode,
            onNavigateBack = onNavigateBack,
        )
    }
}

internal fun NavController.navigateToProfileActivity() {
    navigate(route = ProfileAllActivityDestination())
}
