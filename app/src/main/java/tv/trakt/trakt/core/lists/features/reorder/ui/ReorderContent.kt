@file:OptIn(ExperimentalFoundationApi::class)

package tv.trakt.trakt.core.lists.features.reorder.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.ColorImage
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import kotlinx.collections.immutable.persistentListOf
import tv.trakt.trakt.LocalBottomBarVisibility
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.components.buttons.PrimaryButton
import tv.trakt.trakt.ui.theme.TraktTheme

/** Non-reorderable items above the list (the title bar) that shift LazyList indices. */
private const val HEADER_COUNT = 1

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ReorderContent(
    state: ReorderUiState,
    modifier: Modifier = Modifier,
    onMove: (from: Int, to: Int) -> Unit = { _, _ -> },
    onMoveToTop: (index: Int) -> Unit = {},
    onMoveToBottom: (index: Int) -> Unit = {},
    onMoveToPosition: (index: Int) -> Unit = {},
    onApplyClick: () -> Unit = {},
    onBackClick: () -> Unit = {},
) {
    val listState = rememberLazyListState()
    val listItems = state.items ?: EmptyImmutableList
    val loading = state.loading

    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    val statusBarHeight = WindowInsets.statusBars.asPaddingValues()
        .calculateTopPadding()

    // The status bar covers the top of the list and the main menu bar floats over the bottom, so
    // auto-scroll has to start at their inner edges rather than at the edges of the screen.
    val bottomBarVisible = LocalBottomBarVisibility.current.value
    val menuHeight = WindowInsets.navigationBars.asPaddingValues()
        .calculateBottomPadding()
        .plus(
            when {
                bottomBarVisible -> TraktTheme.size.navigationBarHeight
                else -> 0.dp
            },
        )
    val density = LocalDensity.current
    val edgeInsets = remember(statusBarHeight, menuHeight, density) {
        with(density) {
            DragEdgeInsets(
                start = statusBarHeight.toPx(),
                end = menuHeight.toPx(),
            )
        }
    }

    val dragDropState = rememberDragDropState(
        lazyListState = listState,
        scope = scope,
        edgeInsets = edgeInsets,
        // Exclude the header; only reorderable cards carry an explicit String key.
        draggable = { it.key is String },
        onMove = { fromIndex, toIndex ->
            onMove(fromIndex - HEADER_COUNT, toIndex - HEADER_COUNT)
        },
    )

    val reordering = dragDropState.draggingItemIndex != null

    LaunchedEffect(dragDropState) {
        while (true) {
            val diff = dragDropState.scrollChannel.receive()
            listState.scrollBy(diff)
        }
    }

    val contentPadding = PaddingValues(
        start = TraktTheme.spacing.mainPageHorizontalSpace,
        end = TraktTheme.spacing.mainPageHorizontalSpace,
        top = statusBarHeight,
        bottom = WindowInsets.navigationBars.asPaddingValues()
            .calculateBottomPadding()
            .plus(TraktTheme.size.navigationBarHeight * 2),
    )

    LazyColumn(
        state = listState,
        verticalArrangement = spacedBy(0.dp),
        contentPadding = contentPadding,
        overscrollEffect = null,
        modifier = modifier.fillMaxSize(),
    ) {
        item {
            TitleBar(
                title = state.title,
                applyEnabled = state.items != null && !loading && state.changed,
                onApplyClick = {
                    if (!reordering) {
                        onApplyClick()
                    }
                },
                onBackClick = onBackClick,
                modifier = Modifier.padding(bottom = TraktTheme.spacing.mainListVerticalSpace),
            )
        }

        if (listItems.isNotEmpty()) {
            itemsIndexed(
                items = listItems,
                key = { _, item -> item.key },
            ) { index, item ->
                val lazyIndex = index + HEADER_COUNT

                DraggableItem(
                    dragDropState = dragDropState,
                    index = lazyIndex,
                ) { isDragging ->
                    val indexState = rememberUpdatedState(lazyIndex)
                    var menuExpanded by remember { mutableStateOf(false) }

                    Box {
                        ListReorderMediaCard(
                            rank = "#${index + 1}",
                            title = item.title,
                            subtitle = item.subtitle,
                            contentImageUrl = item.posterUrl,
                            containerImageUrl = item.backdropUrl,
                            shadow = when {
                                isDragging -> 0.dp
                                else -> TraktTheme.colors.shadowDynamicDefault
                            },
                            handleModifier = Modifier.dragHandle(
                                state = dragDropState,
                                index = indexState,
                                haptic = haptic,
                            ),
                            onClick = when {
                                loading -> null
                                else -> ({ menuExpanded = true })
                            },
                            modifier = Modifier
                                .alpha(if (loading) 0.33F else 1F)
                                .padding(bottom = 12.dp),
                        )

                        // Zero-size anchor at the card centre so the menu opens centred on it.
                        Box(modifier = Modifier.align(Alignment.Center)) {
                            ReorderItemMenu(
                                expanded = menuExpanded,
                                onDismiss = { menuExpanded = false },
                                onMoveToTop = {
                                    menuExpanded = false
                                    onMoveToTop(index)
                                },
                                onMoveToBottom = {
                                    menuExpanded = false
                                    onMoveToBottom(index)
                                },
                                onMoveToPosition = {
                                    menuExpanded = false
                                    onMoveToPosition(index)
                                },
                            )
                        }
                    }
                }
            }
        }

        if (loading && listItems.isEmpty()) {
            items(12) {
                ListReorderMediaSkeletonCard(
                    modifier = Modifier
                        .padding(bottom = 12.dp)
                        .animateItem(
                            fadeInSpec = null,
                            fadeOutSpec = null,
                        ),
                )
            }
        }
    }
}

@Composable
private fun ReorderItemMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onMoveToTop: () -> Unit,
    onMoveToBottom: () -> Unit,
    onMoveToPosition: () -> Unit,
) {
    DropdownMenu(
        expanded = expanded,
        containerColor = TraktTheme.colors.dialogContainer,
        shape = RoundedCornerShape(16.dp),
        onDismissRequest = onDismiss,
    ) {
        ReorderMenuItem(
            text = stringResource(R.string.button_text_move_to_top),
            icon = R.drawable.ic_cheveron_down,
            iconRotate = 180F,
            onClick = onMoveToTop,
        )
        ReorderMenuItem(
            text = stringResource(R.string.button_text_move_to_position),
            icon = R.drawable.ic_chevron_all,
            onClick = onMoveToPosition,
        )
        ReorderMenuItem(
            text = stringResource(R.string.button_text_move_to_bottom),
            icon = R.drawable.ic_cheveron_down,
            onClick = onMoveToBottom,
        )
    }
}

@Composable
private fun ReorderMenuItem(
    text: String,
    @DrawableRes icon: Int,
    iconRotate: Float = 0F,
    onClick: () -> Unit,
) {
    DropdownMenuItem(
        text = {
            Text(
                text = text,
                style = TraktTheme.typography.buttonTertiary,
                color = TraktTheme.colors.textPrimary,
            )
        },
        onClick = onClick,
        leadingIcon = {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = TraktTheme.colors.textPrimary,
                modifier = Modifier
                    .size(22.dp)
                    .rotate(iconRotate),
            )
        },
    )
}

@Composable
private fun TitleBar(
    title: String,
    applyEnabled: Boolean,
    modifier: Modifier = Modifier,
    onApplyClick: () -> Unit,
    onBackClick: () -> Unit,
) {
    Row(
        verticalAlignment = CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxWidth()
            .height(TraktTheme.size.titleBarHeight)
            .graphicsLayer {
                translationX = -2.dp.toPx()
            },
    ) {
        Row(
            verticalAlignment = CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .weight(1F, fill = false)
                .padding(end = 16.dp)
                .onClick {
                    onBackClick()
                },
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_back_arrow),
                tint = TraktTheme.colors.textPrimary,
                contentDescription = null,
            )

            TraktHeader(
                title = title,
                subtitle = stringResource(R.string.drawer_title_reorder_list_more),
                modifier = Modifier.weight(1F, fill = false),
            )
        }

        PrimaryButton(
            text = stringResource(R.string.button_text_apply),
            textStyle = TraktTheme.typography.buttonTertiary,
            height = 34.dp,
            corner = 12.dp,
            enabled = applyEnabled,
            onClick = onApplyClick,
        )
    }
}

@OptIn(ExperimentalCoilApi::class)
@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
    locale = "en",
)
@Composable
private fun Preview() {
    TraktTheme {
        val previewHandler = AsyncImagePreviewHandler {
            ColorImage(Color.Blue.toArgb())
        }
        CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
            ReorderContent(
                state = ReorderUiState(
                    title = "My List",
                    changed = true,
                    items = persistentListOf(
                        ReorderItem(
                            key = "1",
                            title = "Lorem Ipsum",
                            subtitle = "2026",
                            posterUrl = "https://example.com/poster.jpg",
                            backdropUrl = null,
                        ),
                        ReorderItem(
                            key = "2",
                            title = "Dolor Sit Amet",
                            subtitle = "2025",
                            posterUrl = null,
                            backdropUrl = null,
                        ),
                    ),
                ),
            )
        }
    }
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
    locale = "en",
)
@Composable
private fun LoadingPreview() {
    TraktTheme {
        ReorderContent(
            state = ReorderUiState(
                title = "My List",
                loading = true,
            ),
        )
    }
}
