package tv.trakt.app.tv.core.people.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.app.tv.common.model.TraktId
import tv.trakt.app.tv.core.people.PersonDetailsScreen

@Serializable
internal data class PersonDestination(
    val personId: Int,
    val sourceId: Int,
    val backdropUrl: String?,
)

internal fun NavGraphBuilder.personDetailsScreen(
    onNavigateToShow: (TraktId) -> Unit,
    onNavigateToMovie: (TraktId) -> Unit,
) {
    composable<PersonDestination> {
        PersonDetailsScreen(
            viewModel = koinViewModel(),
            onShowClick = onNavigateToShow,
            onMovieClick = onNavigateToMovie,
        )
    }
}

internal fun NavController.navigateToPerson(destination: PersonDestination) {
    navigate(route = destination)
}
