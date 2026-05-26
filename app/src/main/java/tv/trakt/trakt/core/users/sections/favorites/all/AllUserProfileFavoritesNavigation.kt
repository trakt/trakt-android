package tv.trakt.trakt.core.users.sections.favorites.all

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import tv.trakt.trakt.common.model.TraktId

@Serializable
internal data class AllUserProfileFavoritesDestination(
    val userId: Int,
)

internal fun NavGraphBuilder.allUserProfileFavoritesScreen(
    onNavigateToShow: (TraktId) -> Unit,
    onNavigateToMovie: (TraktId) -> Unit,
    onNavigateBack: () -> Unit,
) {
    composable<AllUserProfileFavoritesDestination> {
        AllUserProfileFavoritesScreen(
            onNavigateToShow = onNavigateToShow,
            onNavigateToMovie = onNavigateToMovie,
            onNavigateBack = onNavigateBack,
        )
    }
}

internal fun NavController.navigateToAllUserProfileFavorites(userId: TraktId) {
    navigate(route = AllUserProfileFavoritesDestination(userId = userId.value))
}
