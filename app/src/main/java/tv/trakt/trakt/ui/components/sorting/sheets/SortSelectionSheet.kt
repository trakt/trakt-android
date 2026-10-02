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
import tv.trakt.trakt.common.model.sorting.SortOption
import tv.trakt.trakt.common.model.sorting.SortOrder
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
    SortSelectionSheet(
        state = state,
        active = active,
        selectedType = selectedSorting?.type,
        selectedOrder = selectedSorting?.order,
        typeOptions = typeOptions,
        onResult = { type, order ->
            onResult(Sorting(type = type, order = order))
        },
        onDismiss = onDismiss,
    )
}

@Composable
internal fun <T : SortOption> SortSelectionSheet(
    state: SheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
    ),
    active: Boolean = false,
    selectedType: T?,
    selectedOrder: SortOrder?,
    typeOptions: ImmutableList<T>,
    onResult: (type: T, order: SortOrder) -> Unit,
    onDismiss: () -> Unit,
) {
    var currentType by remember(selectedType) {
        mutableStateOf(selectedType)
    }
    var currentOrder by remember(selectedOrder) {
        mutableStateOf(selectedOrder)
    }

    if (active) {
        TraktBottomSheet(
            sheetState = state,
            onDismiss = {
                val type = currentType
                val order = currentOrder
                if (type != null && order != null) {
                    onResult(type, order)
                }
                onDismiss()
            },
        ) {
            SortSelectionView(
                selectedType = currentType,
                selectedOrder = currentOrder,
                typeOptions = typeOptions,
                onSortClick = { type ->
                    if (currentType != null) {
                        currentType = type
                    }
                },
                onOrderClick = { order ->
                    if (currentOrder != null) {
                        currentOrder = order
                    }
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
