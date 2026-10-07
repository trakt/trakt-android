package tv.trakt.trakt.core.profile.sections.leaderboard

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.persistentListOf
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.leaderboard.LeaderboardEntry
import tv.trakt.trakt.core.home.views.HomeEmptySocialView
import tv.trakt.trakt.core.profile.sections.leaderboard.ui.LeaderboardItemView
import tv.trakt.trakt.core.profile.sections.leaderboard.ui.LeaderboardItemViewSkeleton
import tv.trakt.trakt.helpers.SimpleScrollConnection
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.ScrollableBackdropImage
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.theme.TraktTheme

private val TITLE_BAR_BOTTOM_TRIM = 8.dp

@Composable
internal fun LeaderboardScreen(
    modifier: Modifier = Modifier,
    viewModel: LeaderboardViewModel = koinViewModel(),
    onNavigateBack: () -> Unit,
    onUserClick: (User) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LeaderboardContent(
        state = state,
        modifier = modifier,
        onUserClick = onUserClick,
        onEndOfList = viewModel::loadMoreData,
        onBackClick = onNavigateBack,
    )
}

@Composable
private fun LeaderboardContent(
    state: LeaderboardState,
    modifier: Modifier = Modifier,
    onUserClick: (User) -> Unit = {},
    onEndOfList: () -> Unit = {},
    onBackClick: () -> Unit = {},
) {
    val items = state.items ?: EmptyImmutableList
    val listState = rememberLazyListState()

    val listScrollConnection = rememberSaveable(saver = SimpleScrollConnection.Saver) {
        SimpleScrollConnection()
    }

    val isScrolledToBottom by remember(items.size) {
        derivedStateOf {
            val info = listState.layoutInfo
            val lastVisible = info.visibleItemsInfo.lastOrNull()?.index ?: 0
            info.totalItemsCount > 0 && lastVisible >= info.totalItemsCount - 5
        }
    }

    LaunchedEffect(isScrolledToBottom) {
        if (isScrolledToBottom) {
            onEndOfList()
        }
    }

    val firstFreeIndex = remember(items) {
        items.indexOfFirst { it.isLocked && !it.isViewer }
    }

    val contentPadding = PaddingValues(
        start = TraktTheme.spacing.mainPageHorizontalSpace,
        end = TraktTheme.spacing.mainPageHorizontalSpace,
        top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
        bottom = WindowInsets.navigationBars.asPaddingValues()
            .calculateBottomPadding()
            .plus(TraktTheme.size.navigationBarHeight * 2),
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TraktTheme.colors.backgroundPrimary)
            .nestedScroll(listScrollConnection),
    ) {
        ScrollableBackdropImage(
            translation = listScrollConnection.resultOffset,
        )

        LazyColumn(
            state = listState,
            verticalArrangement = spacedBy(8.dp),
            contentPadding = contentPadding,
            overscrollEffect = null,
        ) {
            item {
                TitleBar(
                    modifier = Modifier
                        .padding(top = 3.dp)
                        .onClick { onBackClick() },
                )
            }

            if (state.loading.isLoading && items.isEmpty()) {
                items(count = 12) {
                    LeaderboardItemViewSkeleton(
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            } else {
                itemsIndexed(
                    items = items,
                    key = { _, entry -> entry.user.ids.trakt.value },
                ) { index, entry ->
                    if (index == firstFreeIndex) {
                        Text(
                            text = stringResource(R.string.text_leaderboard_free_section).uppercase(),
                            style = TraktTheme.typography.meta,
                            color = TraktTheme.colors.textSecondary,
                            modifier = Modifier.padding(top = 12.dp, bottom = 8.dp),
                        )
                    }

                    LeaderboardItemView(
                        entry = entry,
                        onClick = { onUserClick(entry.user) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateItem(
                                fadeInSpec = null,
                                fadeOutSpec = null,
                            ),
                    )
                }

                if (state.loadingMore.isLoading) {
                    item {
                        LeaderboardItemViewSkeleton(
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }

            if (state.error != null) {
                item {
                    Text(
                        text = "${
                            stringResource(R.string.error_text_unexpected_error_short)
                        }\n\n${state.error}",
                        color = TraktTheme.colors.textSecondary,
                        style = TraktTheme.typography.meta,
                        maxLines = 10,
                    )
                }
            } else if (state.loading == Done && items.isEmpty()) {
                item {
                    HomeEmptySocialView(
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Composable
private fun TitleBar(modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = CenterVertically,
        horizontalArrangement = spacedBy(12.dp),
        modifier = modifier
            .layout { measurable, constraints ->
                // Reports a shorter height so the list starts closer, while the content
                // keeps its position within the full-height bar.
                val placeable = measurable.measure(constraints)
                val trim = TITLE_BAR_BOTTOM_TRIM.roundToPx()
                layout(placeable.width, placeable.height - trim) {
                    placeable.place(0, 0)
                }
            }
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
        TraktHeader(
            title = stringResource(R.string.header_follows_leaderboard),
        )
    }
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview() {
    TraktTheme {
        LeaderboardContent(
            state = LeaderboardState(
                loading = Done,
                items = persistentListOf(
                    LeaderboardEntry(
                        user = PreviewData.user1.copy(isVip = true),
                        rank = 1,
                        totalMinutes = 523_400,
                        totalPlays = 12_400,
                        isLocked = false,
                        isViewer = true,
                    ),
                    LeaderboardEntry(
                        user = PreviewData.user1.copy(
                            ids = PreviewData.user1.ids.copy(trakt = TraktId(2)),
                            name = "Rip Wheeler",
                        ),
                        rank = null,
                        totalMinutes = null,
                        totalPlays = null,
                        isLocked = true,
                    ),
                ),
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
private fun PreviewLoading() {
    TraktTheme {
        LeaderboardContent(
            state = LeaderboardState(
                loading = Loading,
            ),
        )
    }
}
