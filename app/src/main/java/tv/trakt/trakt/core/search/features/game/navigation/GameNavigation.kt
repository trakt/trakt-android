package tv.trakt.trakt.core.search.features.game.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import tv.trakt.trakt.core.search.features.game.GameScreen

@Serializable
internal data object GameDestination

internal fun NavGraphBuilder.gameScreen(onNavigateBack: () -> Unit) {
    composable<GameDestination> {
        GameScreen(
            onNavigateBack = onNavigateBack,
        )
    }
}

internal fun NavController.navigateToGame() {
    navigate(route = GameDestination)
}
