package tv.trakt.trakt.core.lists.features.all.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.common.model.CustomList
import tv.trakt.trakt.core.lists.features.all.AllListsScreen
import tv.trakt.trakt.core.lists.sections.personal.model.PersonalListType

@Serializable
internal data class AllListsDestination(
    val initialFilter: PersonalListType?,
)

internal fun NavGraphBuilder.allListsScreen(
    onNavigateToList: (CustomList) -> Unit,
    onNavigateToPersonalList: (CustomList) -> Unit,
    onNavigateBack: () -> Unit,
) {
    composable<AllListsDestination> {
        AllListsScreen(
            viewModel = koinViewModel(),
            onNavigateList = onNavigateToList,
            onNavigatePersonalList = onNavigateToPersonalList,
            onNavigateBack = onNavigateBack,
        )
    }
}

internal fun NavController.navigateToAllLists(initialFilter: PersonalListType? = null) {
    navigate(
        route = AllListsDestination(initialFilter),
    )
}
