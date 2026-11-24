package tv.trakt.trakt.core.lists.features.convert.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.lists.SmartList
import tv.trakt.trakt.core.lists.features.convert.SmartListConvertScreen

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
