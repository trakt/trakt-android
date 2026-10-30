package tv.trakt.trakt.core.profile.sections.leaderboard.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.core.profile.sections.leaderboard.LeaderboardScreen

@Serializable
internal data class LeaderboardDestination(
    val followingCount: Int? = null,
)

internal fun NavGraphBuilder.leaderboardScreen(
    onNavigateToUser: (User) -> Unit,
    onNavigateBack: () -> Unit,
) {
    composable<LeaderboardDestination> { backStackEntry ->
        val destination = backStackEntry.toRoute<LeaderboardDestination>()
        LeaderboardScreen(
            followingCount = destination.followingCount,
            viewModel = koinViewModel(),
            onUserClick = onNavigateToUser,
            onNavigateBack = onNavigateBack,
        )
    }
}

internal fun NavController.navigateToLeaderboard(followingCount: Int?) {
    navigate(route = LeaderboardDestination(followingCount = followingCount))
}
