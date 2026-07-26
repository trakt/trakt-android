package tv.trakt.trakt.app.core.lists.filters

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.app.common.ui.buttons.PrimaryButton
import tv.trakt.trakt.app.helpers.extensions.requestSafeFocus
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.common.model.globalfilter.GlobalFilterMode
import tv.trakt.trakt.common.model.sorting.Sorting
import tv.trakt.trakt.resources.R

@Immutable
internal data class TvListControlsState(
    val filter: GlobalFilter,
    val sorting: Sorting,
    val configuration: TvListFilterConfiguration,
)

@Composable
internal fun TvListControls(
    state: TvListControlsState,
    modifier: Modifier = Modifier,
    onFilterApplied: (GlobalFilter) -> Unit,
    onSortingApplied: (Sorting) -> Unit,
) {
    var activeDialog by remember { mutableStateOf<TvListDialog?>(null) }
    var filterMode by rememberSaveable { mutableStateOf(GlobalFilterMode.Simple) }
    var restoreFocusTo by remember { mutableStateOf<TvListDialog?>(null) }
    val filterFocusRequester = remember { FocusRequester() }
    val sortingFocusRequester = remember { FocusRequester() }

    LaunchedEffect(activeDialog, restoreFocusTo) {
        if (activeDialog != null) return@LaunchedEffect

        when (restoreFocusTo) {
            TvListDialog.Filters -> filterFocusRequester.requestSafeFocus()
            TvListDialog.Sorting -> sortingFocusRequester.requestSafeFocus()
            null -> Unit
        }
        restoreFocusTo = null
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(TraktTheme.spacing.mainGridSpace),
        modifier = modifier,
    ) {
        PrimaryButton(
            text = stringResource(R.string.button_label_filters),
            icon = painterResource(
                when (state.filter.isActive) {
                    true -> R.drawable.ic_filter_on
                    false -> R.drawable.ic_filter_off
                },
            ),
            containerColor = when (state.filter.isActive) {
                true -> TraktTheme.colors.accent
                false -> TraktTheme.colors.chipContainer
            },
            onClick = {
                activeDialog = TvListDialog.Filters
            },
            modifier = Modifier
                .width(ListControlWidth)
                .focusRequester(filterFocusRequester),
        )

        PrimaryButton(
            text = stringResource(state.sorting.type.displayStringRes),
            icon = painterResource(state.sorting.order.displayIconRes),
            containerColor = TraktTheme.colors.chipContainer,
            onClick = {
                activeDialog = TvListDialog.Sorting
            },
            modifier = Modifier
                .width(ListControlWidth)
                .focusRequester(sortingFocusRequester),
        )
    }

    if (activeDialog == TvListDialog.Filters) {
        TvListFilterDialog(
            appliedFilter = state.filter,
            initialMode = filterMode,
            configuration = state.configuration,
            onApply = { filter, mode ->
                filterMode = mode
                onFilterApplied(filter)
                restoreFocusTo = TvListDialog.Filters
                activeDialog = null
            },
            onDismiss = {
                restoreFocusTo = TvListDialog.Filters
                activeDialog = null
            },
        )
    }

    if (activeDialog == TvListDialog.Sorting) {
        TvListSortDialog(
            appliedSorting = state.sorting,
            onApply = { sorting ->
                onSortingApplied(sorting)
                restoreFocusTo = TvListDialog.Sorting
                activeDialog = null
            },
            onDismiss = {
                restoreFocusTo = TvListDialog.Sorting
                activeDialog = null
            },
        )
    }
}

private enum class TvListDialog {
    Filters,
    Sorting,
}

// Keeps the two controls a stable width as their localized labels change.
private val ListControlWidth = 148.dp
