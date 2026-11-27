package tv.trakt.trakt.core.mixtape.navigation

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.mixtape.MixtapeItem
import tv.trakt.trakt.core.mixtape.MixtapeScreen
import tv.trakt.trakt.core.mixtape.results.MixtapeResultsScreen

internal const val MIXTAPE_SOURCE_POSTER_MENU = "poster-menu"

@Serializable
internal data class MixtapeDestination(
    val type: MediaType? = null,
    val pick: Int? = null,
    val source: String? = null,
)

internal val LocalStartMixtape = staticCompositionLocalOf<((MediaType, TraktId) -> Unit)?> { null }

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

internal fun NavController.navigateToMixtape(
    type: MediaType? = null,
    pick: TraktId? = null,
    source: String? = null,
) {
    navigate(
        route = MixtapeDestination(
            type = type,
            pick = pick?.value,
            source = source,
        ),
    )
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
    onNavigateStartOver: (MediaType) -> Unit,
) {
    composable<MixtapeResultsDestination> {
        MixtapeResultsScreen(
            viewModel = koinViewModel(),
            onNavigateBack = onNavigateBack,
            onNavigateClose = onNavigateClose,
            onNavigateStartOver = onNavigateStartOver,
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
    popBackStack<MixtapeDestination>(inclusive = true)
}

internal fun NavController.restartMixtape(type: MediaType) {
    closeMixtape()
    navigateToMixtape(type = type)
}
