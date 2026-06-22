@file:OptIn(ExperimentalFoundationApi::class)

package tv.trakt.trakt.core.lists.features.reorder

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarDuration.Long
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.ColorImage
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.LocalSnackbarState
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.extensions.toLocal
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.common.ui.theme.colors.Red400
import tv.trakt.trakt.core.lists.features.reorder.ui.DraggableItem
import tv.trakt.trakt.core.lists.features.reorder.ui.ListReorderMediaCard
import tv.trakt.trakt.core.lists.features.reorder.ui.ListReorderMediaSkeletonCard
import tv.trakt.trakt.core.lists.features.reorder.ui.dragHandle
import tv.trakt.trakt.core.lists.features.reorder.ui.rememberDragDropState
import tv.trakt.trakt.core.lists.model.CustomListItem.MovieItem
import tv.trakt.trakt.core.lists.model.CustomListItem.ShowItem
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.components.buttons.PrimaryButton
import tv.trakt.trakt.ui.components.confirmation.ConfirmationSheet
import tv.trakt.trakt.ui.theme.TraktTheme
import java.time.Instant

/** Non-reorderable items above the list (the title bar) that shift LazyList indices. */
private const val HEADER_COUNT = 1

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ListReorderScreen(
    modifier: Modifier = Modifier,
    viewModel: ListReorderViewModel,
    onNavigateBack: () -> Unit,
) {
    val snack = LocalSnackbarState.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    var showExitConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(state.error) {
        if (state.error != null) {
            snack.showSnackbar(
                message = state.error?.localizedMessage ?: "",
                duration = Long,
            )
            viewModel.clearError()
        }
    }

    LaunchedEffect(state.done) {
        if (state.done) {
            onNavigateBack()
        }
    }

    val hasUnsavedChanges = state.items != null &&
        state.items?.map { it.itemId } != state.initialItemsOrder

    val handleBack = {
        if (hasUnsavedChanges) {
            showExitConfirm = true
        } else {
            onNavigateBack()
        }
    }

    BackHandler(enabled = hasUnsavedChanges) {
        showExitConfirm = true
    }

    ListReorderContent(
        state = state,
        modifier = modifier,
        onMove = viewModel::reorderItem,
        onApplyClick = viewModel::applyChanges,
        onBackClick = handleBack,
    )

    ConfirmationSheet(
        active = showExitConfirm,
        title = stringResource(R.string.dialog_title_discard_changes),
        message = stringResource(R.string.warning_prompt_discard_changes),
        yesText = stringResource(R.string.button_text_discard),
        yesColor = Red400,
        onYes = {
            showExitConfirm = false
            onNavigateBack()
        },
        onNo = {
            showExitConfirm = false
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ListReorderContent(
    state: ListReorderState,
    modifier: Modifier = Modifier,
    onMove: (from: Int, to: Int) -> Unit = { _, _ -> },
    onApplyClick: () -> Unit = {},
    onBackClick: () -> Unit = {},
) {
    val listState = rememberLazyListState()
    val listItems = (state.items ?: emptyList()).toImmutableList()
    val loading = state.loading.isLoading
    val sameOrder = remember(state.items, state.initialItemsOrder) {
        state.items?.map { it.itemId } == state.initialItemsOrder
    }

    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    val dragDropState = rememberDragDropState(
        lazyListState = listState,
        scope = scope,
        // Exclude the header (Int key); only reorderable cards carry a String key.
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
        top = WindowInsets.statusBars.asPaddingValues()
            .calculateTopPadding(),
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
                title = state.list?.name ?: "",
                applyEnabled = state.items != null && !loading && !sameOrder,
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
                val subtitle = item.released?.toLocal()?.year?.toString().orEmpty()
                val lazyIndex = index + HEADER_COUNT

                DraggableItem(
                    dragDropState = dragDropState,
                    index = lazyIndex,
                ) { isDragging ->
                    val indexState = rememberUpdatedState(lazyIndex)

                    ListReorderMediaCard(
                        rank = "#${index + 1}",
                        title = item.title,
                        subtitle = subtitle,
                        contentImageUrl = item.images?.getPosterUrl(),
                        containerImageUrl = item.images?.getFanartUrl(Images.Size.THUMB),
                        shadow = when {
                            isDragging -> 4.dp
                            else -> 0.dp
                        },
                        handleModifier = Modifier.dragHandle(
                            state = dragDropState,
                            index = indexState,
                            haptic = haptic,
                        ),
                        modifier = Modifier
                            .alpha(if (loading) 0.33F else 1F)
                            .padding(bottom = 12.dp),
                    )
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
                subtitle = stringResource(R.string.drawer_title_reorder_list),
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
            ListReorderContent(
                state = ListReorderState(
                    loading = LoadingState.Done,
                    list = PreviewData.customList1,
                    items = persistentListOf(
                        ShowItem(
                            show = PreviewData.show1,
                            itemId = 1,
                            rank = 1,
                            listedAt = Instant.EPOCH,
                        ),
                        MovieItem(
                            movie = PreviewData.movie1,
                            rank = 2,
                            itemId = 2,
                            listedAt = Instant.EPOCH,
                        ),
                    ),
                ),
            )
        }
    }
}
