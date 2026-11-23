package tv.trakt.trakt.core.lists.sections.smart.details.recipe

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue.Expanded
import androidx.compose.material3.SheetValue.Hidden
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.model.lists.AwardCeremony
import tv.trakt.trakt.common.model.lists.SmartList
import tv.trakt.trakt.ui.components.TraktBottomSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SmartListRecipeSheet(
    state: SheetState = rememberBottomSheetState(
        initialValue = Hidden,
        enabledValues = setOf(Hidden, Expanded),
    ),
    list: SmartList?,
    awardCeremonies: ImmutableList<AwardCeremony>,
    onDismiss: () -> Unit,
) {
    if (list != null) {
        TraktBottomSheet(
            sheetState = state,
            contentWindowInsets = { WindowInsets() },
            onDismiss = onDismiss,
        ) {
            SmartListRecipeView(
                recipe = list.rememberRecipe(awardCeremonies),
                listName = list.name,
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .padding(top = 8.dp),
            )
        }
    }
}
