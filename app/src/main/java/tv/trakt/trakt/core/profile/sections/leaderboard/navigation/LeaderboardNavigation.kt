package tv.trakt.trakt.core.profile.sections.leaderboard.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.core.profile.sections.leaderboard.LeaderboardScreen

@Serializable
internal data object LeaderboardDestination

internal fun NavGraphBuilder.leaderboardScreen(
    onNavigateToUser: (User) -> Unit,
    onNavigateBack: () -> Unit,
) {
    composable<LeaderboardDestination> {
        LeaderboardScreen(
            viewModel = koinViewModel(),
            onUserClick = onNavigateToUser,
            onNavigateBack = onNavigateBack,
        )
    }
}

internal fun NavController.navigateToLeaderboard() {
    navigate(route = LeaderboardDestination)
}
