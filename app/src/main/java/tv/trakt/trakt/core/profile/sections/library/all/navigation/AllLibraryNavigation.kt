package tv.trakt.trakt.core.profile.sections.library.all.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.profile.sections.library.all.AllLibraryScreen

@Serializable
internal data class AllLibraryDestination(
    val dummy: Boolean = false,
)

internal fun NavGraphBuilder.allLibraryScreen(
    onNavigateToShow: (TraktId) -> Unit,
    onNavigateToMovie: (TraktId) -> Unit,
    onNavigateBack: () -> Unit,
) {
    composable<AllLibraryDestination> {
        AllLibraryScreen(
            viewModel = koinViewModel(),
            onShowClick = onNavigateToShow,
            onMovieClick = onNavigateToMovie,
            onNavigateBack = onNavigateBack,
        )
    }
}

internal fun NavController.navigateToLibrary() {
    navigate(route = AllLibraryDestination())
}
