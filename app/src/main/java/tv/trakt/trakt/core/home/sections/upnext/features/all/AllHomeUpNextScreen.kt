@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package tv.trakt.trakt.core.home.sections.upnext.features.all

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.layout.LazyLayoutCacheWindow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType.Companion.Confirm
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.helpers.extensions.durationFormat
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Images.Size.THUMB
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.ui.composables.FilmProgressIndicator
import tv.trakt.trakt.core.home.sections.upnext.features.context.sheets.UpNextItemContextSheet
import tv.trakt.trakt.core.home.sections.upnext.model.ProgressShow
import tv.trakt.trakt.helpers.rememberHeaderState
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.EpisodeProgressBar
import tv.trakt.trakt.ui.components.ScrollableBackdropImage
import tv.trakt.trakt.ui.components.dateselection.DateSelectionResult
import tv.trakt.trakt.ui.components.dateselection.DateSelectionSheet
import tv.trakt.trakt.ui.components.mediacards.PanelMediaCard
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun AllHomeUpNextScreen(
    modifier: Modifier = Modifier,
    viewModel: AllHomeUpNextViewModel = koinViewModel(),
    onNavigateToShow: (TraktId) -> Unit,
    onNavigateToEpisode: (showId: TraktId, episode: Episode) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current

    var contextSheet by remember { mutableStateOf<ProgressShow?>(null) }
    var dateSheet by remember { mutableStateOf<ProgressShow?>(null) }

    LaunchedEffect(state.info) {
        if (state.info != null) {
            haptic.performHapticFeedback(Confirm)
            viewModel.clearInfo()
        }
    }

    AllHomeUpNextContent(
        state = state,
        modifier = modifier,
        onLoadMore = { viewModel.loadMoreData() },
        onClick = {
            if (!it.loading) {
                onNavigateToEpisode(
                    it.show.ids.trakt,
                    it.progress.nextEpisode,
                )
            }
        },
        onLongClick = {
            if (!it.loading) {
                contextSheet = it
            }
        },
        onCheckClick = {
            viewModel.addToHistory(it.id)
        },
        onCheckLongClick = {
            dateSheet = it
        },
        onShowClick = {
            if (!it.loading) {
                onNavigateToShow(it.show.ids.trakt)
            }
        },
        onBackClick = onNavigateBack,
    )

    UpNextItemContextSheet(
        sheetItem = contextSheet,
        onDismiss = { contextSheet = null },
        onAddWatched = {
            dateSheet = contextSheet
        },
        onDropShow = {
            viewModel.removeShow(it.show.ids.trakt)
        },
    )

    HomeDateSelectionSheet(
        item = dateSheet,
        onDateSelected = { date ->
            val episode = dateSheet?.progress?.nextEpisode
                ?: return@HomeDateSelectionSheet

            viewModel.addToHistory(
                episodeId = episode.ids.trakt,
                customDate = date,
            )
        },
        onDismiss = {
            dateSheet = null
        },
    )
}

@Composable
internal fun AllHomeUpNextContent(
    state: AllHomeUpNextState,
    modifier: Modifier = Modifier,
    onClick: (ProgressShow) -> Unit = {},
    onLongClick: (ProgressShow) -> Unit = {},
    onCheckClick: (ProgressShow) -> Unit = {},
    onCheckLongClick: (ProgressShow) -> Unit = {},
    onShowClick: (ProgressShow) -> Unit = {},
    onBackClick: () -> Unit = {},
    onLoadMore: () -> Unit = {},
) {
    val headerState = rememberHeaderState()
    val listState = rememberLazyListState(
        cacheWindow = LazyLayoutCacheWindow(
            aheadFraction = 0.5F,
            behindFraction = 0.5F,
        ),
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TraktTheme.colors.backgroundPrimary)
            .nestedScroll(headerState.connection),
    ) {
        val contentPadding = PaddingValues(
            start = TraktTheme.spacing.mainPageHorizontalSpace,
            end = TraktTheme.spacing.mainPageHorizontalSpace,
            top = WindowInsets.statusBars.asPaddingValues()
                .calculateTopPadding(),
            bottom = WindowInsets.navigationBars.asPaddingValues()
                .calculateBottomPadding()
                .plus(TraktTheme.size.navigationBarHeight * 2),
        )

        ScrollableBackdropImage(
            scrollState = listState,
        )

        ContentList(
            listState = listState,
            listItems = (state.items ?: emptyList()).toImmutableList(),
            contentPadding = contentPadding,
            loadingMore = state.loadingMore.isLoading,
            onTopOfList = { headerState.resetScrolled() },
            onEndOfList = onLoadMore,
            onClick = onClick,
            onLongClick = onLongClick,
            onCheckClick = onCheckClick,
            onCheckLongClick = onCheckLongClick,
            onShowClick = onShowClick,
            onBackClick = onBackClick,
        )
    }
}

@Composable
private fun ContentList(
    modifier: Modifier = Modifier,
    listItems: ImmutableList<ProgressShow>,
    listState: LazyListState,
    contentPadding: PaddingValues,
    loadingMore: Boolean,
    onTopOfList: () -> Unit,
    onEndOfList: () -> Unit,
    onClick: (ProgressShow) -> Unit,
    onLongClick: (ProgressShow) -> Unit,
    onCheckClick: (ProgressShow) -> Unit,
    onCheckLongClick: (ProgressShow) -> Unit,
    onShowClick: (ProgressShow) -> Unit,
    onBackClick: () -> Unit,
) {
    val isScrolledToBottom by remember(listItems.size) {
        derivedStateOf {
            listState.firstVisibleItemIndex >= (listItems.size - 5)
        }
    }

    val isScrolledToTop by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex == 0 &&
                listState.firstVisibleItemScrollOffset == 0
        }
    }

    LaunchedEffect(isScrolledToTop) {
        if (isScrolledToTop) {
            onTopOfList()
        }
    }

    LaunchedEffect(isScrolledToBottom) {
        if (isScrolledToBottom) {
            onEndOfList()
        }
    }

    LazyColumn(
        state = listState,
        verticalArrangement = spacedBy(0.dp),
        contentPadding = contentPadding,
        overscrollEffect = null,
        modifier = modifier,
    ) {
        item {
            TitleBar(
                modifier = Modifier
                    .padding(bottom = 2.dp)
                    .onClick { onBackClick() },
            )
        }

        items(
            items = listItems,
            key = { it.key },
        ) { item ->
            ContentListItem(
                item = item,
                onClick = { onClick(item) },
                onLongClick = { onLongClick(item) },
                onCheckClick = { onCheckClick(item) },
                onCheckLongClick = { onCheckLongClick(item) },
                onShowClick = { onShowClick(item) },
                modifier = Modifier.animateItem(
                    fadeInSpec = null,
                    fadeOutSpec = null,
                ),
            )
        }

        if (loadingMore) {
            item {
                FilmProgressIndicator(
                    size = 32.dp,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun ContentListItem(
    item: ProgressShow,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onCheckClick: () -> Unit,
    onCheckLongClick: () -> Unit,
    onShowClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PanelMediaCard(
        title = item.show.title,
        titleOriginal = item.show.titleOriginal,
        subtitle = item.progress.nextEpisode.seasonEpisodeString(),
        contentImageUrl = item.show.images?.getPosterUrl(),
        containerImageUrl = item.progress.nextEpisode.images?.getScreenshotUrl(THUMB)
            ?: item.show.images?.getFanartUrl(THUMB),
        onClick = onClick,
        onLongClick = onLongClick,
        onImageClick = onShowClick,
        footerContent = {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                val startString = remember {
                    buildString {
                        val runtime = item.progress.nextEpisode.runtime?.inWholeMinutes
                            ?: item.show.runtime?.inWholeMinutes

                        if (runtime != null) {
                            append(runtime.durationFormat())
                        }
                    }
                }

                val remainingEpisodesString = stringResource(
                    R.string.tag_text_remaining_episodes,
                    item.progress.remainingEpisodes,
                )

                val endString = remember {
                    val separator = "  •  "
                    buildString {
                        val remainingEpisodes = item.progress.remainingEpisodes
                        if (remainingEpisodes > 0) {
                            append(remainingEpisodesString)
                        }

                        append(separator)

                        val remainingTime = item.progress.remainingMinutesString
                        if (remainingTime != null) {
                            append(remainingTime)
                        }
                    }
                }

                val remainingPercent = remember(
                    item.progress.completed,
                    item.progress.aired,
                ) {
                    item.progress.remainingPercent
                }

                EpisodeProgressBar(
                    startText = startString,
                    endText = endString,
                    progress = remainingPercent,
                    containerColor = TraktTheme.colors.chipContainer,
                    modifier = Modifier
                        .weight(1F, fill = false)
                        .padding(end = 16.dp),
                )

                if (item.loading) {
                    Box(modifier = Modifier.size(18.dp)) {
                        FilmProgressIndicator(size = 16.dp)
                    }
                } else {
                    Icon(
                        painter = painterResource(R.drawable.ic_check),
                        contentDescription = null,
                        tint = TraktTheme.colors.accent,
                        modifier = Modifier
                            .size(18.dp)
                            .combinedClickable(
                                onClick = onCheckClick,
                                onLongClick = onCheckLongClick,
                                interactionSource = null,
                                indication = null,
                            ),
                    )
                }
            }
        },
        modifier = modifier
            .padding(bottom = TraktTheme.spacing.mainListVerticalSpace),
    )
}

@Composable
private fun TitleBar(modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = CenterVertically,
        horizontalArrangement = spacedBy(12.dp),
        modifier = modifier
            .height(TraktTheme.size.titleBarHeight)
            .graphicsLayer {
                translationX = -2.dp.toPx()
            },
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_back_arrow),
            tint = TraktTheme.colors.textPrimary,
            contentDescription = null,
        )
        Text(
            text = stringResource(R.string.list_title_up_next),
            color = TraktTheme.colors.textPrimary,
            style = TraktTheme.typography.heading5,
        )
    }
}

@Composable
private fun HomeDateSelectionSheet(
    item: ProgressShow?,
    onDateSelected: (DateSelectionResult?) -> Unit,
    onDismiss: () -> Unit,
) {
    DateSelectionSheet(
        active = item != null,
        title = item?.show?.title.orEmpty(),
        subtitle = item?.progress?.nextEpisode?.seasonEpisodeString(),
        onResult = {
            if (item == null) return@DateSelectionSheet
            onDateSelected(it)
        },
        onDismiss = onDismiss,
    )
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview() {
    TraktTheme {
        AllHomeUpNextContent(
            state = AllHomeUpNextState(
                loading = LoadingState.DONE,
            ),
        )
    }
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview2() {
    TraktTheme {
        AllHomeUpNextContent(
            state = AllHomeUpNextState(
                loading = LOADING,
            ),
        )
    }
}
