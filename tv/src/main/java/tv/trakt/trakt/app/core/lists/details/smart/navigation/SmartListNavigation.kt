package tv.trakt.trakt.app.core.lists.details.smart.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.app.core.lists.details.smart.SmartListScreen
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.lists.SmartList

@Serializable
internal data class SmartListDestination(
    val listJson: String,
)

internal fun NavGraphBuilder.smartListScreen(
    onNavigateToShow: (TraktId) -> Unit,
    onNavigateToMovie: (TraktId) -> Unit,
) {
    composable<SmartListDestination> {
        SmartListScreen(
            viewModel = koinViewModel(),
            onNavigateToShow = onNavigateToShow,
            onNavigateToMovie = onNavigateToMovie,
        )
    }
}

internal fun NavController.navigateToSmartList(list: SmartList) {
    navigate(
        route = SmartListDestination(
            listJson = Json.encodeToString(list),
        ),
    )
}
