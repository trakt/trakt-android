package tv.trakt.trakt.tv.core.details.lists.details.shows.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.tv.common.model.CustomList
import tv.trakt.trakt.tv.common.model.TraktId
import tv.trakt.trakt.tv.core.details.lists.details.shows.CustomListShowsScreen

@Serializable
internal data class CustomListShowsDestination(
    val listId: Int,
    val listName: String,
)

internal fun NavGraphBuilder.customListShows(onNavigateToShow: (TraktId) -> Unit) {
    composable<CustomListShowsDestination> {
        CustomListShowsScreen(
            viewModel = koinViewModel(),
            onNavigateToShow = onNavigateToShow,
        )
    }
}

internal fun NavController.navigateToCustomListShows(list: CustomList) {
    navigate(
        route = CustomListShowsDestination(
            listId = list.ids.trakt.value,
            listName = list.name,
        ),
    )
}
