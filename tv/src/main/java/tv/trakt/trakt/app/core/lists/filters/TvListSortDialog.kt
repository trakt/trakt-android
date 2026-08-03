package tv.trakt.trakt.app.core.lists.filters

import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.tv.material3.Text
import tv.trakt.trakt.app.common.ui.buttons.PrimaryButton
import tv.trakt.trakt.app.helpers.extensions.requestSafeFocus
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.common.model.sorting.SortOrder
import tv.trakt.trakt.common.model.sorting.SortType
import tv.trakt.trakt.common.model.sorting.Sorting
import tv.trakt.trakt.resources.R

@Composable
internal fun TvListSortDialog(
    appliedSorting: Sorting,
    modifier: Modifier = Modifier,
    onApply: (Sorting) -> Unit,
    onDismiss: () -> Unit,
) {
    var draft by remember(appliedSorting) {
        mutableStateOf(appliedSorting)
    }
    val selectedFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        selectedFocusRequester.requestSafeFocus()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(DialogSectionSpacing),
            modifier = modifier
                .fillMaxWidth(0.52F)
                .fillMaxHeight(0.82F)
                .background(
                    color = TraktTheme.colors.dialogContainer,
                    shape = RoundedCornerShape(DialogCornerRadius),
                )
                .padding(DialogContentPadding),
        ) {
            Text(
                text = stringResource(R.string.drawer_title_sort),
                color = TraktTheme.colors.textPrimary,
                style = TraktTheme.typography.heading4,
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(TraktTheme.spacing.mainGridSpace),
                modifier = Modifier
                    .weight(1F)
                    .fillMaxWidth()
                    .focusGroup(),
            ) {
                items(
                    items = SortType.entries,
                    key = SortType::value,
                ) { type ->
                    val selected = draft.type == type
                    PrimaryButton(
                        text = stringResource(type.displayStringRes),
                        textAllCaps = false,
                        icon = type.displayIconRes?.let { painterResource(it) },
                        containerColor = when (selected) {
                            true -> TraktTheme.colors.accent
                            false -> TraktTheme.colors.chipContainer
                        },
                        onClick = {
                            draft = draft.copy(type = type)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(
                                when (selected) {
                                    true -> Modifier.focusRequester(selectedFocusRequester)
                                    false -> Modifier
                                },
                            ),
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(TraktTheme.spacing.mainGridSpace),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusGroup(),
            ) {
                SortOrder.entries.forEach { order ->
                    val selected = draft.order == order
                    PrimaryButton(
                        text = stringResource(order.displayStringRes),
                        icon = painterResource(order.displayIconRes),
                        containerColor = when (selected) {
                            true -> TraktTheme.colors.accent
                            false -> TraktTheme.colors.chipContainer
                        },
                        onClick = {
                            draft = draft.copy(order = order)
                        },
                        modifier = Modifier.weight(1F),
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(TraktTheme.spacing.mainGridSpace),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusGroup(),
            ) {
                PrimaryButton(
                    text = stringResource(R.string.button_text_cancel),
                    containerColor = TraktTheme.colors.chipContainer,
                    onClick = onDismiss,
                    modifier = Modifier.weight(1F),
                )
                PrimaryButton(
                    text = stringResource(R.string.button_text_apply),
                    onClick = { onApply(draft) },
                    modifier = Modifier.weight(2F),
                )
            }
        }
    }
}

private val DialogContentPadding = 24.dp
private val DialogSectionSpacing = 16.dp
private val DialogCornerRadius = 20.dp
