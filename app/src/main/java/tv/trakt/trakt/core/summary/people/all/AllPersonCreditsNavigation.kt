package tv.trakt.trakt.core.summary.people.all

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.lists.CustomList
import tv.trakt.trakt.core.summary.people.model.PersonCreditsSection

@Serializable
internal data class AllPersonCreditsDestination(
    val personId: Int,
    val section: PersonCreditsSection,
    val position: String? = null,
    val filterJson: String? = null,
)

internal fun NavGraphBuilder.allPersonCreditsScreen(
    onNavigateToShow: (TraktId) -> Unit,
    onNavigateToMovie: (TraktId) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToList: (CustomList) -> Unit,
) {
    composable<AllPersonCreditsDestination> {
        AllPersonCreditsScreen(
            onNavigateToShow = onNavigateToShow,
            onNavigateToMovie = onNavigateToMovie,
            onNavigateBack = onNavigateBack,
            onNavigateToList = onNavigateToList,
        )
    }
}

internal fun NavController.navigateToAllPersonCredits(destination: AllPersonCreditsDestination) {
    navigate(route = destination)
}
