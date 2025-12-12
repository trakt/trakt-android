package tv.trakt.trakt.core.discover.sections.all.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.discover.model.DiscoverSection
import tv.trakt.trakt.core.discover.sections.all.AllDiscoverScreen

@Serializable
internal data class DiscoverDestination(
    val source: DiscoverSection,
)

internal fun NavGraphBuilder.discoverAllScreen(
    customThemeEnabled: Boolean,
    onNavigateBack: () -> Unit,
    onNavigateToShow: (TraktId) -> Unit,
    onNavigateToMovie: (TraktId) -> Unit,
) {
    composable<DiscoverDestination> {
        AllDiscoverScreen(
            viewModel = koinViewModel {
                parametersOf(customThemeEnabled)
            },
            onNavigateToShow = onNavigateToShow,
            onNavigateToMovie = onNavigateToMovie,
            onNavigateBack = onNavigateBack,
        )
    }
}

internal fun NavController.navigateToDiscoverAll(source: DiscoverSection) {
    navigate(
        route = DiscoverDestination(
            source = source,
        ),
    )
}
