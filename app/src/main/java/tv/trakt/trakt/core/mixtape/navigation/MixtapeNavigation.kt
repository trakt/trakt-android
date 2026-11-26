package tv.trakt.trakt.core.mixtape.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.core.mixtape.MixtapeScreen

@Serializable
internal data object MixtapeDestination

internal fun NavGraphBuilder.mixtapeScreen(onNavigateBack: () -> Unit) {
    composable<MixtapeDestination> {
        MixtapeScreen(
            viewModel = koinViewModel(),
            onNavigateBack = onNavigateBack,
        )
    }
}

internal fun NavController.navigateToMixtape() {
    navigate(route = MixtapeDestination)
}
