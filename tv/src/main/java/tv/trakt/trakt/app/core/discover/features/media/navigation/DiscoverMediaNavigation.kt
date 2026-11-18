package tv.trakt.trakt.app.core.discover.features.media.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.app.core.discover.features.media.DiscoverMediaViewAllScreen
import tv.trakt.trakt.app.core.discover.model.DiscoverMediaSection
import tv.trakt.trakt.common.model.TraktId

@Serializable
internal data class DiscoverMediaDestination(
    val section: DiscoverMediaSection,
)

internal fun NavGraphBuilder.discoverMediaScreen(
    onNavigateToShow: (TraktId) -> Unit,
    onNavigateToMovie: (TraktId) -> Unit,
) {
    composable<DiscoverMediaDestination> {
        DiscoverMediaViewAllScreen(
            viewModel = koinViewModel(),
            onNavigateToShow = onNavigateToShow,
            onNavigateToMovie = onNavigateToMovie,
        )
    }
}

internal fun NavController.navigateToDiscoverMedia(section: DiscoverMediaSection) {
    navigate(route = DiscoverMediaDestination(section))
}
