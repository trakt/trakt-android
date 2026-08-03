package tv.trakt.trakt.core.lists.sections.smart.details.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.lists.SmartList
import tv.trakt.trakt.core.lists.sections.smart.details.SmartListDetailsScreen

@Serializable
internal data class SmartListDetailsDestination(
    val listJson: String,
)

internal fun NavGraphBuilder.smartListDetailsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToShow: (TraktId) -> Unit,
    onNavigateToMovie: (TraktId) -> Unit,
) {
    composable<SmartListDetailsDestination> {
        SmartListDetailsScreen(
            viewModel = koinViewModel(),
            onNavigateBack = onNavigateBack,
            onShowClick = onNavigateToShow,
            onMovieClick = onNavigateToMovie,
        )
    }
}

internal fun NavController.navigateToSmartListDetails(list: SmartList) {
    navigate(
        route = SmartListDetailsDestination(
            listJson = Json.encodeToString(list),
        ),
    )
}
