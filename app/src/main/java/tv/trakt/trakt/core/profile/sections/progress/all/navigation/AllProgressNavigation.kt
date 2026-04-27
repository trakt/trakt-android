package tv.trakt.trakt.core.profile.sections.progress.all.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.profile.sections.progress.all.AllProgressScreen

@Serializable
internal data class AllProgressDestination(
    val unused: Boolean = false,
)

internal fun NavGraphBuilder.allProgressScreen(
    onNavigateToShow: (TraktId) -> Unit,
    onNavigateBack: () -> Unit,
) {
    composable<AllProgressDestination> {
        AllProgressScreen(
            viewModel = koinViewModel(),
            onShowClick = onNavigateToShow,
            onNavigateBack = onNavigateBack,
        )
    }
}

internal fun NavController.navigateToProgress() {
    navigate(route = AllProgressDestination())
}
