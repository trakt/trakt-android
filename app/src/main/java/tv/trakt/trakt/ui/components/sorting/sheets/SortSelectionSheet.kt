@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.ui.components.sorting.sheets

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import tv.trakt.trakt.common.model.sorting.SortTypeList
import tv.trakt.trakt.ui.components.TraktBottomSheet
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun SortSelectionSheet(
    state: SheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
    ),
    active: Boolean = false,
    selected: SortTypeList? = null,
    options: ImmutableList<SortTypeList> = SortTypeList.entries.toImmutableList(),
    onResult: (sort: SortTypeList) -> Unit,
    onDismiss: () -> Unit,
) {
    val scope = rememberCoroutineScope()

    if (active) {
        TraktBottomSheet(
            sheetState = state,
            onDismiss = onDismiss,
        ) {
            SortSelectionView(
                selected = selected,
                options = options,
                onSortClick = { selected ->
                    scope.dismissWithAction(
                        sheet = state,
                        action = { onResult(selected) },
                        onDismiss = onDismiss,
                    )
                },
                modifier = Modifier
                    .padding(bottom = 24.dp)
                    .padding(horizontal = 24.dp),
            )
        }
    }
}

private fun CoroutineScope.dismissWithAction(
    sheet: SheetState,
    action: () -> Unit = {},
    onDismiss: () -> Unit,
) {
    launch {
        sheet.hide()
    }.invokeOnCompletion {
        if (!sheet.isVisible) {
            action()
            onDismiss()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
    TraktTheme {
        SortSelectionSheet(
            state = rememberModalBottomSheetState(
                skipPartiallyExpanded = true,
            ),
            active = true,
            selected = SortTypeList.DEFAULT,
            onResult = { },
            onDismiss = { },
        )
    }
}
