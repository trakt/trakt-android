package tv.trakt.trakt.app.core.profile.sections.library.viewall.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.app.core.profile.sections.library.viewall.ProfileLibraryViewAllScreen
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.TraktId

@Serializable
internal data object ProfileLibraryViewAllDestination

internal fun NavGraphBuilder.profileLibraryViewAllScreen(
    onNavigateToMovie: (TraktId) -> Unit,
    onNavigateToEpisode: (TraktId, Episode) -> Unit,
) {
    composable<ProfileLibraryViewAllDestination> {
        ProfileLibraryViewAllScreen(
            viewModel = koinViewModel(),
            onNavigateToMovie = onNavigateToMovie,
            onNavigateToEpisode = onNavigateToEpisode,
        )
    }
}

internal fun NavController.navigateToProfileLibraryViewAll() {
    navigate(
        route = ProfileLibraryViewAllDestination,
    )
}
