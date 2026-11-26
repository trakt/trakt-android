package tv.trakt.trakt.core.mixtape.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.mixtape.MixtapeItem
import tv.trakt.trakt.core.mixtape.MixtapeScreen
import tv.trakt.trakt.core.mixtape.results.MixtapeResultsScreen

@Serializable
internal data object MixtapeDestination

internal fun NavGraphBuilder.mixtapeScreen(
    onNavigateBack: () -> Unit,
    onNavigateToResults: (MediaType, List<MixtapeItem>) -> Unit,
) {
    composable<MixtapeDestination> {
        MixtapeScreen(
            viewModel = koinViewModel(),
            onNavigateBack = onNavigateBack,
            onNavigateToResults = onNavigateToResults,
        )
    }
}

internal fun NavController.navigateToMixtape() {
    navigate(route = MixtapeDestination)
}

@Serializable
internal data class MixtapeResultsDestination(
    val type: MediaType,
    val picks: List<Int>,
    val titles: List<String>,
)

internal fun NavGraphBuilder.mixtapeResultsScreen(
    onNavigateBack: () -> Unit,
    onNavigateClose: () -> Unit,
) {
    composable<MixtapeResultsDestination> {
        MixtapeResultsScreen(
            viewModel = koinViewModel(),
            onNavigateBack = onNavigateBack,
            onNavigateClose = onNavigateClose,
        )
    }
}

internal fun NavController.navigateToMixtapeResults(
    type: MediaType,
    picks: List<MixtapeItem>,
) {
    navigate(
        route = MixtapeResultsDestination(
            type = type,
            picks = picks.map { it.id.value },
            titles = picks.map { it.title },
        ),
    )
}

internal fun NavController.closeMixtape() {
    popBackStack(route = MixtapeDestination, inclusive = true)
}
