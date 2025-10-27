package tv.trakt.trakt.core.profile.sections.favorites.all.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.profile.sections.favorites.all.AllFavoritesScreen

@Serializable
internal data class AllFavoritesDestination(
    val homeWatchlist: Boolean = false,
)

internal fun NavGraphBuilder.allFavoritesScreen(
    onNavigateToShow: (TraktId) -> Unit,
    onNavigateToMovie: (TraktId) -> Unit,
    onNavigateBack: () -> Unit,
) {
    composable<AllFavoritesDestination> {
        AllFavoritesScreen(
            viewModel = koinViewModel(),
            onShowClick = onNavigateToShow,
            onMovieClick = onNavigateToMovie,
            onNavigateBack = onNavigateBack,
        )
    }
}

internal fun NavController.navigateToFavorites() {
    navigate(route = AllFavoritesDestination())
}
