package tv.trakt.trakt.core.summary.people.all

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.summary.people.model.PersonCreditsSection

@Serializable
internal data class AllPersonCreditsDestination(
    val personId: Int,
    val section: PersonCreditsSection,
    val position: String? = null,
)

internal fun NavGraphBuilder.allPersonCreditsScreen(
    onNavigateToShow: (TraktId) -> Unit,
    onNavigateToMovie: (TraktId) -> Unit,
    onNavigateBack: () -> Unit,
) {
    composable<AllPersonCreditsDestination> {
        AllPersonCreditsScreen(
            onNavigateToShow = onNavigateToShow,
            onNavigateToMovie = onNavigateToMovie,
            onNavigateBack = onNavigateBack,
        )
    }
}

internal fun NavController.navigateToAllPersonCredits(destination: AllPersonCreditsDestination) {
    navigate(route = destination)
}
