package tv.trakt.trakt.app.core.details.lists.details.media.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.app.core.details.lists.details.media.CustomListMediaScreen
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.lists.CustomList

@Serializable
internal data class CustomListMediaDestination(
    val listId: Int,
    val listName: String,
    val listLikes: Int,
    val listAuthor: String,
    val listItemCount: Int?,
    val listType: String?,
)

internal fun NavGraphBuilder.customListMedia(
    onNavigateToShow: (TraktId) -> Unit,
    onNavigateToMovie: (TraktId) -> Unit,
) {
    composable<CustomListMediaDestination> {
        CustomListMediaScreen(
            viewModel = koinViewModel(),
            onNavigateToShow = onNavigateToShow,
            onNavigateToMovie = onNavigateToMovie,
        )
    }
}

internal fun NavController.navigateToCustomListMedia(
    list: CustomList,
    type: MediaType?,
) {
    navigate(
        route = CustomListMediaDestination(
            listId = list.ids.trakt.value,
            listName = list.name,
            listLikes = list.likes ?: 0,
            listAuthor = list.user.displayName,
            listItemCount = list.itemCount,
            listType = type?.value,
        ),
    )
}
