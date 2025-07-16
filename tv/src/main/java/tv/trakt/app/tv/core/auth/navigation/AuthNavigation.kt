package tv.trakt.app.tv.core.auth.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.app.tv.core.auth.AuthScreen
import tv.trakt.app.tv.helpers.extensions.popUpToTop

@Serializable
internal data object AuthDestination

internal fun NavGraphBuilder.authScreen(onAuthorized: () -> Unit) {
    composable<AuthDestination> {
        AuthScreen(
            viewModel = koinViewModel(),
            onAuthorized = onAuthorized,
        )
    }
}

internal fun NavController.navigateToAuth() {
    navigate(route = AuthDestination) {
        popUpToTop(this@navigateToAuth)
    }
}
