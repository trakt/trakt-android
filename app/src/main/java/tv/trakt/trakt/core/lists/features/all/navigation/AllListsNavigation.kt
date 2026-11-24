package tv.trakt.trakt.core.lists.features.all.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.lists.CustomList
import tv.trakt.trakt.common.model.lists.SmartList
import tv.trakt.trakt.common.model.lists.SmartListFilters
import tv.trakt.trakt.core.lists.features.all.AllListsScreen
import tv.trakt.trakt.core.lists.sections.personal.model.PersonalListType

@Serializable
internal data class AllListsDestination(
    val initialFilter: PersonalListType?,
    val templates: Boolean = false,
)

internal fun NavGraphBuilder.allListsScreen(
    onNavigateToList: (CustomList) -> Unit,
    onNavigateToPersonalList: (CustomList) -> Unit,
    onNavigateToSmartList: (SmartList) -> Unit,
    onNavigateToCreateSmartList: () -> Unit,
    onNavigateToEditSmartList: (SmartList) -> Unit,
    onNavigateToSmartListTemplate: (SmartListFilters, String) -> Unit,
    onNavigateToListReorder: (CustomList) -> Unit,
    onNavigateToAddFromLists: (CustomList) -> Unit,
    onNavigateToListsReorder: () -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToUser: (User) -> Unit,
) {
    composable<AllListsDestination> { entry ->
        AllListsScreen(
            viewModel = koinViewModel(),
            templatesViewModel = koinViewModel(),
            scrollToTemplates = entry.toRoute<AllListsDestination>().templates,
            onNavigateList = onNavigateToList,
            onNavigatePersonalList = onNavigateToPersonalList,
            onNavigateSmartList = onNavigateToSmartList,
            onNavigateCreateSmartList = onNavigateToCreateSmartList,
            onNavigateEditSmartList = onNavigateToEditSmartList,
            onNavigateSmartListTemplate = onNavigateToSmartListTemplate,
            onNavigateListReorder = onNavigateToListReorder,
            onNavigateAddFromLists = onNavigateToAddFromLists,
            onNavigateListsReorder = onNavigateToListsReorder,
            onNavigateBack = onNavigateBack,
            onNavigateUser = onNavigateToUser,
        )
    }
}

internal fun NavController.navigateToAllLists(
    initialFilter: PersonalListType?,
    templates: Boolean = false,
) {
    navigate(
        route = AllListsDestination(
            initialFilter = initialFilter,
            templates = templates,
        ),
    )
}
