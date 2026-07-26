package tv.trakt.trakt.app.core.lists.filters

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.app.common.ui.buttons.IconButton
import tv.trakt.trakt.app.common.ui.buttons.PrimaryButton
import tv.trakt.trakt.app.helpers.extensions.requestSafeFocus
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.common.model.MediaMode
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
    val sortingOrderContentDescription = stringResource(state.sorting.order.displayStringRes)
    val filtersContentDescription = stringResource(R.string.button_label_filters)

    LaunchedEffect(activeDialog, restoreFocusTo) {
        if (activeDialog != null) return@LaunchedEffect

        when (restoreFocusTo) {
            TvListDialog.Filters -> filterFocusRequester.requestSafeFocus()
            TvListDialog.Sorting -> sortingFocusRequester.requestSafeFocus()
            null -> Unit
        }
        restoreFocusTo = null
    }

    Box(
        modifier = modifier,
    ) {
        if (state.configuration.allowedMediaModes.size > 1) {
            TvMediaModeSelector(
                selected = state.filter.mode,
                options = state.configuration.allowedMediaModes,
                onSelect = { mode ->
                    onFilterApplied(state.filter.copy(mode = mode))
                },
                modifier = Modifier.align(Alignment.Center),
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(TraktTheme.spacing.mainGridSpace),
            modifier = Modifier
                .align(Alignment.CenterEnd),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(SplitButtonSpacing),
            ) {
                PrimaryButton(
                    text = stringResource(state.sorting.type.displayStringRes),
                    textAllCaps = false,
                    containerColor = TraktTheme.colors.chipContainer,
                    onClick = {
                        activeDialog = TvListDialog.Sorting
                    },
                    modifier = Modifier
                        .width(SortTypeControlWidth)
                        .focusRequester(sortingFocusRequester),
                )

                IconButton(
                    icon = painterResource(state.sorting.order.displayIconRes),
                    size = HeaderControlSize,
                    iconSize = HeaderControlIconSize,
                    containerColor = TraktTheme.colors.chipContainer,
                    focusedScale = HEADER_CONTROL_FOCUSED_SCALE,
                    onClick = {
                        onSortingApplied(
                            state.sorting.copy(order = state.sorting.order.toggle()),
                        )
                    },
                    modifier = Modifier.semantics {
                        contentDescription = sortingOrderContentDescription
                    },
                )
            }

            Box {
                IconButton(
                    icon = painterResource(
                        when (state.filter.isActive) {
                            true -> R.drawable.ic_filter_on
                            false -> R.drawable.ic_filter_off
                        },
                    ),
                    size = HeaderControlSize,
                    iconSize = HeaderControlIconSize,
                    containerColor = Color.Transparent,
                    focusedScale = HEADER_CONTROL_FOCUSED_SCALE,
                    onClick = {
                        activeDialog = TvListDialog.Filters
                    },
                    modifier = Modifier
                        .focusRequester(filterFocusRequester)
                        .semantics {
                            contentDescription = filtersContentDescription
                        },
                )

                if (state.filter.isActive) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(FilterIndicatorInset)
                            .size(FilterIndicatorSize)
                            .clip(CircleShape)
                            .background(TraktTheme.colors.accent),
                    )
                }
            }
        }
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

@Composable
private fun TvMediaModeSelector(
    selected: MediaMode,
    modifier: Modifier = Modifier,
    options: ImmutableList<MediaMode>,
    onSelect: (MediaMode) -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(MediaSelectorSpacing),
        modifier = modifier
            .background(
                color = TraktTheme.colors.chipContainer,
                shape = CircleShape,
            )
            .padding(MediaSelectorPadding),
    ) {
        options.forEach { option ->
            val isSelected = option == selected
            val label = stringResource(option.displayRes)
            val shape = CircleShape
            val focusedBorder = Border(
                border = BorderStroke(
                    width = MediaSelectorFocusedBorderWidth,
                    color = TraktTheme.colors.textPrimary,
                ),
                shape = shape,
            )

            Button(
                contentPadding = PaddingValues(horizontal = MediaSelectorContentPadding),
                shape = ButtonDefaults.shape(
                    shape = shape,
                    focusedDisabledShape = shape,
                ),
                border = ButtonDefaults.border(
                    focusedBorder = focusedBorder,
                ),
                colors = ButtonDefaults.colors(
                    containerColor = when (isSelected) {
                        true -> TraktTheme.colors.accent
                        false -> Color.Transparent
                    },
                    contentColor = TraktTheme.colors.textPrimary,
                    focusedContainerColor = when (isSelected) {
                        true -> TraktTheme.colors.accent
                        false -> Color.Transparent
                    },
                    focusedContentColor = TraktTheme.colors.textPrimary,
                    pressedContainerColor = TraktTheme.colors.accent,
                    pressedContentColor = TraktTheme.colors.textPrimary,
                ),
                scale = ButtonDefaults.scale(
                    focusedScale = MEDIA_SELECTOR_FOCUSED_SCALE,
                ),
                onClick = {
                    if (!isSelected) {
                        onSelect(option)
                    }
                },
                modifier = Modifier
                    .height(MediaSelectorButtonHeight)
                    .semantics {
                        this.selected = isSelected
                    },
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(MediaSelectorContentSpacing),
                ) {
                    Icon(
                        painter = painterResource(
                            when (isSelected) {
                                true -> option.onIcon
                                false -> option.offIcon
                            },
                        ),
                        contentDescription = when (isSelected) {
                            true -> null
                            false -> label
                        },
                        tint = TraktTheme.colors.textPrimary,
                        modifier = Modifier.size(MediaSelectorIconSize),
                    )

                    if (isSelected) {
                        Text(
                            text = label,
                            color = TraktTheme.colors.textPrimary,
                            style = TraktTheme.typography.buttonTertiary,
                        )
                    }
                }
            }
        }
    }
}

private enum class TvListDialog {
    Filters,
    Sorting,
}

// Header-only dimensions keep the phone-inspired control group compact and stable on TV.
private val SortTypeControlWidth = 132.dp
private val HeaderControlSize = 42.dp
private val HeaderControlIconSize = 20.dp
private const val HEADER_CONTROL_FOCUSED_SCALE = 1.04F
private val SplitButtonSpacing = 3.dp
private val FilterIndicatorInset = 4.dp
private val FilterIndicatorSize = 6.dp
private val MediaSelectorSpacing = 2.dp
private val MediaSelectorPadding = 3.dp
private val MediaSelectorContentPadding = 10.dp
private val MediaSelectorContentSpacing = 6.dp
private val MediaSelectorButtonHeight = 40.dp
private val MediaSelectorIconSize = 18.dp
private val MediaSelectorFocusedBorderWidth = 2.75.dp
private const val MEDIA_SELECTOR_FOCUSED_SCALE = 1.04F
