@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.ui.components.sorting.sheets

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.model.sorting.SortType
import tv.trakt.trakt.common.model.sorting.Sorting
import tv.trakt.trakt.ui.components.TraktBottomSheet
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun SortSelectionSheet(
    state: SheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
    ),
    active: Boolean = false,
    selectedSorting: Sorting? = null,
    typeOptions: ImmutableList<SortType> = SortType.entries.toImmutableList(),
    onResult: (sorting: Sorting) -> Unit,
    onDismiss: () -> Unit,
) {
    var currentSorting by remember(selectedSorting) {
        mutableStateOf(selectedSorting)
    }

    if (active) {
        TraktBottomSheet(
            sheetState = state,
            onDismiss = {
                currentSorting?.let { onResult(it) }
                onDismiss()
            },
        ) {
            SortSelectionView(
                selectedType = currentSorting?.type,
                selectedOrder = currentSorting?.order,
                typeOptions = typeOptions,
                onSortClick = { type ->
                    currentSorting = currentSorting
                        ?.copy(type = type)
                },
                onOrderClick = { order ->
                    currentSorting = currentSorting
                        ?.copy(order = order)
                },
                modifier = Modifier
                    .padding(bottom = 24.dp)
                    .padding(horizontal = 24.dp),
            )
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
            selectedSorting = Sorting.RecentlyAdded,
            onResult = { },
            onDismiss = { },
        )
    }
}
