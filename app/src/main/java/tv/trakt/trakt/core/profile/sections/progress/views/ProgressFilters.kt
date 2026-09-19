@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.profile.sections.progress.views

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults.rememberTooltipPositionProvider
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import tv.trakt.trakt.common.core.tutorials.StubTutorialsManager
import tv.trakt.trakt.common.core.tutorials.TutorialsManager
import tv.trakt.trakt.common.core.tutorials.model.TutorialKey.PROGRESS_FILTERS_REORDER
import tv.trakt.trakt.core.profile.sections.progress.model.ProgressFilter
import tv.trakt.trakt.ui.components.chips.FilterChip
import tv.trakt.trakt.ui.components.chips.ReorderableFilterChipGroup
import tv.trakt.trakt.ui.theme.TraktTheme
import kotlin.time.Duration.Companion.seconds

private val DefaultOrder = ProgressFilter.entries.toImmutableList()

/** Reorder mode is a modal gesture state, so it lets go on its own once the row goes quiet. */
private val ReorderTimeout = 6.seconds

@Composable
internal fun ProgressFilters(
    modifier: Modifier = Modifier,
    order: ImmutableList<ProgressFilter> = DefaultOrder,
    selected: ProgressFilter? = null,
    unselectedTextVisible: Boolean = true,
    height: Dp = 28.dp,
    paddingHorizontal: PaddingValues = PaddingValues.Zero,
    paddingVertical: PaddingValues = PaddingValues.Zero,
    onReorder: (ImmutableList<ProgressFilter>) -> Unit = {},
    onClick: (ProgressFilter) -> Unit = {},
) {
    val initialSelected = remember {
        mutableStateOf(selected)
    }

    LaunchedEffect(selected) {
        if (selected != null) {
            initialSelected.value = selected
        }
    }

    val scope = rememberCoroutineScope()
    val tutorials = when {
        LocalInspectionMode.current -> StubTutorialsManager()
        else -> koinInject<TutorialsManager>()
    }

    var reordering by remember { mutableStateOf(false) }
    var interactions by remember { mutableIntStateOf(0) }
    val hintState = rememberTooltipState(isPersistent = true)

    val exitReorder = {
        if (reordering) {
            reordering = false
            if (hintState.isVisible) {
                scope.launch { tutorials.acknowledge(PROGRESS_FILTERS_REORDER) }
            }
        }
    }

    BackHandler(enabled = reordering) {
        exitReorder()
    }

    LaunchedEffect(reordering, interactions) {
        if (!reordering) return@LaunchedEffect
        delay(ReorderTimeout)
        exitReorder()
    }

    LaunchedEffect(reordering) {
        if (!reordering) {
            hintState.dismiss()
            return@LaunchedEffect
        }
        if (tutorials.get(PROGRESS_FILTERS_REORDER)) return@LaunchedEffect
        hintState.show()
    }

    TooltipBox(
        state = hintState,
        positionProvider = rememberTooltipPositionProvider(
            positioning = TooltipAnchorPosition.Above,
        ),
        tooltip = { ProgressFiltersHint() },
        enableUserInput = false,
        modifier = modifier,
    ) {
        ReorderableFilterChipGroup(
            items = order,
            key = { it.name },
            reordering = reordering,
            paddingHorizontal = paddingHorizontal,
            paddingVertical = paddingVertical,
            onEnterReorder = {
                interactions++
                reordering = true
            },
            onReorder = { newOrder ->
                interactions++
                // The hint lives in a popup that swallows the next touch outside it, so it goes as
                // soon as the gesture that it was explaining ends.
                hintState.dismiss()
                scope.launch { tutorials.acknowledge(PROGRESS_FILTERS_REORDER) }
                onReorder(newOrder)
            },
        ) { filter, _, chipModifier ->
            FilterChip(
                selected = selected == filter,
                animated = initialSelected.value != null,
                text = stringResource(filter.displayRes),
                height = height,
                unselectedTextVisible = unselectedTextVisible,
                leadingContent = {
                    Icon(
                        painter = painterResource(filter.iconRes),
                        contentDescription = null,
                        tint = when {
                            selected == filter -> TraktTheme.colors.textPrimaryOnAccent
                            else -> TraktTheme.colors.textPrimary
                        },
                        modifier = Modifier
                            .size(FilterChipDefaults.IconSize)
                            .graphicsLayer {
                                translationX = (-1).dp.toPx()
                            },
                    )
                },
                onClick = {
                    exitReorder()
                    onClick(filter)
                },
                modifier = chipModifier,
            )
        }
    }
}

@Preview
@Composable
private fun Preview() {
    TraktTheme {
        ProgressFilters(
            selected = ProgressFilter.Completed,
        )
    }
}
