package tv.trakt.trakt.app.core.lists.views

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.app.common.ui.buttons.OutlineButton
import tv.trakt.trakt.app.common.ui.buttons.PrimaryButton
import tv.trakt.trakt.app.common.ui.menus.TvDropdownMenu
import tv.trakt.trakt.app.common.ui.menus.TvDropdownMenuItem
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.common.model.sorting.SortType
import tv.trakt.trakt.common.model.sorting.Sorting

private val sortTypeButtonWidth = 200.dp

@Composable
internal fun ListSortControls(
    sorting: Sorting,
    onSortingChange: (Sorting) -> Unit,
    modifier: Modifier = Modifier,
    loading: Boolean = false,
) {
    var menuVisible by remember { mutableStateOf(false) }

    Row(
        horizontalArrangement = spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        Box {
            PrimaryButton(
                text = stringResource(sorting.type.displayStringRes),
                icon = sorting.type.displayIconRes?.let { painterResource(it) },
                loading = loading,
                onClick = { menuVisible = true },
                modifier = Modifier.width(sortTypeButtonWidth),
            )

            SortTypeMenu(
                visible = menuVisible,
                selected = sorting.type,
                onSelect = { onSortingChange(sorting.copy(type = it)) },
                onDismiss = { menuVisible = false },
            )
        }

        OutlineButton(
            icon = painterResource(sorting.order.displayIconRes),
            onClick = {
                onSortingChange(sorting.copy(order = sorting.order.toggle()))
            },
        )
    }
}

@Composable
private fun SortTypeMenu(
    visible: Boolean,
    selected: SortType,
    onSelect: (SortType) -> Unit,
    onDismiss: () -> Unit,
) {
    TvDropdownMenu(
        visible = visible,
        onDismiss = onDismiss,
    ) {
        val selectedFocusRequester = remember { FocusRequester() }
        var focusedType by remember { mutableStateOf(selected) }

        LaunchedEffect(Unit) {
            selectedFocusRequester.requestFocus()
        }

        SortType.entries.forEach { type ->
            TvDropdownMenuItem(
                text = stringResource(type.displayStringRes),
                icon = type.displayIconRes?.let { painterResource(it) },
                focused = focusedType == type,
                onFocus = { focusedType = type },
                onClick = {
                    onDismiss()
                    onSelect(type)
                },
                modifier = when (type) {
                    selected -> Modifier.focusRequester(selectedFocusRequester)
                    else -> Modifier
                },
            )
        }
    }
}

@Preview
@Composable
private fun Preview() {
    TraktTheme {
        ListSortControls(
            sorting = Sorting.RecentlyAdded,
            onSortingChange = {},
        )
    }
}

@Preview
@Composable
private fun PreviewLoading() {
    TraktTheme {
        ListSortControls(
            sorting = Sorting.Default,
            onSortingChange = {},
            loading = true,
        )
    }
}
