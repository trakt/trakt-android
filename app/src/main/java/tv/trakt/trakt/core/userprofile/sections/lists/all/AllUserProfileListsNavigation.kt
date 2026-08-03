package tv.trakt.trakt.core.userprofile.sections.lists.all

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.lists.CustomList
import tv.trakt.trakt.core.lists.sections.personal.model.PersonalListType

@Serializable
internal data class AllUserProfileListsDestination(
    val userId: Int,
    val userName: String,
    val initialFilter: PersonalListType,
)

internal fun NavGraphBuilder.allUserProfileListsScreen(
    onNavigateToList: (CustomList) -> Unit,
    onNavigateBack: () -> Unit,
) {
    composable<AllUserProfileListsDestination> {
        AllUserProfileListsScreen(
            onNavigateToList = onNavigateToList,
            onNavigateBack = onNavigateBack,
        )
    }
}

internal fun NavController.navigateToAllUserProfileLists(
    user: User,
    initialFilter: PersonalListType,
) {
    navigate(
        route = AllUserProfileListsDestination(
            userId = user.ids.trakt.value,
            userName = user.displayName,
            initialFilter = initialFilter,
        ),
    )
}
