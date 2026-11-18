package tv.trakt.trakt.app.core.profile.sections.history.viewall.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.app.core.profile.sections.history.viewall.ProfileHistoryViewAllScreen
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.MediaMode
import tv.trakt.trakt.common.model.TraktId

@Serializable
internal data class ProfileHistoryViewAllDestination(
    val mode: String? = null,
)

internal fun NavGraphBuilder.profileHistoryViewAllScreen(
    onNavigateToMovie: (TraktId) -> Unit,
    onNavigateToEpisode: (TraktId, Episode) -> Unit,
) {
    composable<ProfileHistoryViewAllDestination> {
        ProfileHistoryViewAllScreen(
            viewModel = koinViewModel(),
            onNavigateToMovie = onNavigateToMovie,
            onNavigateToEpisode = onNavigateToEpisode,
        )
    }
}

internal fun NavController.navigateToProfileHistoryViewAll(mode: MediaMode? = null) {
    navigate(
        route = ProfileHistoryViewAllDestination(mode = mode?.name),
    )
}
