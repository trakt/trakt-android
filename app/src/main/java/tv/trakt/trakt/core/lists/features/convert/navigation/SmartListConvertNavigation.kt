package tv.trakt.trakt.core.lists.features.convert.navigation

import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.lists.SmartList
import tv.trakt.trakt.core.lists.features.convert.SmartListConvertScreen
import tv.trakt.trakt.core.lists.features.details.navigation.ListsDetailsDestination
import tv.trakt.trakt.core.lists.sections.personal.features.all.navigation.ListsPersonalDestination
import tv.trakt.trakt.core.lists.sections.smart.details.navigation.navigateToSmartListDetails

@Serializable
internal data class SmartListConvertDestination(
    val listId: Int,
)

internal fun NavGraphBuilder.smartListConvertScreen(
    onNavigateBack: () -> Unit,
    onNavigateToSmartList: (SmartList) -> Unit,
) {
    composable<SmartListConvertDestination> {
        SmartListConvertScreen(
            viewModel = koinViewModel(),
            onNavigateBack = onNavigateBack,
            onConverted = onNavigateToSmartList,
        )
    }
}

internal fun NavController.navigateToSmartListConvert(listId: TraktId) {
    navigate(
        route = SmartListConvertDestination(
            listId = listId.value,
        ),
    )
}

internal fun NavController.navigateToConvertedSmartList(smartList: SmartList) {
    popBackStack()

    val origin = currentBackStackEntry?.destination
    val fromConvertedList = origin?.hasRoute(ListsDetailsDestination::class) == true ||
        origin?.hasRoute(ListsPersonalDestination::class) == true
    if (fromConvertedList) popBackStack()

    navigateToSmartListDetails(smartList)
}
