package tv.trakt.trakt.app.core.lists.details.personal.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.app.common.model.CustomList
import tv.trakt.trakt.app.core.lists.details.personal.PersonalListScreen
import tv.trakt.trakt.common.model.TraktId

@Serializable
internal data class PersonalListDestination(
    val listId: Int,
    val listName: String,
)

internal fun NavGraphBuilder.personalListScreen(
    onNavigateToShow: (TraktId) -> Unit,
    onNavigateToMovie: (TraktId) -> Unit,
) {
    composable<PersonalListDestination> {
        PersonalListScreen(
            viewModel = koinViewModel(),
            onNavigateToShow = onNavigateToShow,
            onNavigateToMovie = onNavigateToMovie,
        )
    }
}

internal fun NavController.navigateToPersonalList(list: CustomList) {
    navigate(
        route = PersonalListDestination(
            listId = list.ids.trakt.value,
            listName = list.name,
        ),
    )
}
