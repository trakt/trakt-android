package tv.trakt.trakt.core.younify.features.younify.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.core.settings.features.younify.YounifyScreen

@Serializable
internal data object YounifyDestination

internal fun NavGraphBuilder.younifyScreen(onNavigateBack: () -> Unit) {
    composable<YounifyDestination> {
        YounifyScreen(
            viewModel = koinViewModel(),
            onNavigateBack = onNavigateBack,
        )
    }
}

internal fun NavController.navigateToYounify() {
    navigate(route = YounifyDestination)
}
