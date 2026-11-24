package tv.trakt.trakt.core.lists.sections.smart.details.recipe

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue.Expanded
import androidx.compose.material3.SheetValue.Hidden
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import tv.trakt.trakt.common.model.lists.SmartList
import tv.trakt.trakt.ui.components.TraktBottomSheet
import kotlin.random.Random.Default.nextInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SmartListRecipeSheet(
    state: SheetState = rememberBottomSheetState(
        initialValue = Hidden,
        enabledValues = setOf(Hidden, Expanded),
    ),
    list: SmartList?,
    onDismiss: () -> Unit,
) {
    val viewModelKey = remember(list) { nextInt().toString() }

    if (list != null) {
        val viewModel: SmartListRecipeViewModel = koinViewModel(
            key = viewModelKey,
            parameters = { parametersOf(list) },
        )
        val recipeState by viewModel.state.collectAsStateWithLifecycle()

        TraktBottomSheet(
            sheetState = state,
            contentWindowInsets = { WindowInsets() },
            onDismiss = onDismiss,
        ) {
            SmartListRecipeView(
                recipe = list.rememberRecipe(recipeState.awardCeremonies),
                listName = list.name,
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .padding(top = 8.dp),
            )
        }
    }
}
