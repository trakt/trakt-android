package tv.trakt.trakt.app.core.profile.sections.history.viewall.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.app.core.profile.sections.history.viewall.ProfileHistoryViewAllScreen
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.TraktId

@Serializable
internal data object ProfileHistoryViewAllDestination

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

internal fun NavController.navigateToProfileHistoryViewAll() {
    navigate(
        route = ProfileHistoryViewAllDestination,
    )
}
