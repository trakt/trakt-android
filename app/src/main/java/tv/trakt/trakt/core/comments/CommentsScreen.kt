@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package tv.trakt.trakt.core.comments

import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.layout.LazyLayoutCacheWindow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.model.Comment
import tv.trakt.trakt.core.comments.details.CommentDetailsSheet
import tv.trakt.trakt.core.comments.model.CommentsFilter
import tv.trakt.trakt.core.comments.ui.CommentCard
import tv.trakt.trakt.core.comments.ui.CommentSkeletonCard
import tv.trakt.trakt.helpers.rememberHeaderState
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.FilterChip
import tv.trakt.trakt.ui.components.FilterChipGroup
import tv.trakt.trakt.ui.components.ScrollableBackdropImage
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun CommentsScreen(
    modifier: Modifier = Modifier,
    viewModel: CommentsViewModel,
    onNavigateBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    var commentSheet by remember { mutableStateOf<Comment?>(null) }

    CommentsContent(
        state = state,
        modifier = modifier,
        onClick = {
            commentSheet = it
        },
        onFilterClick = {
            viewModel.setFilter(it)
        },
        onBackClick = onNavigateBack,
    )

    CommentDetailsSheet(
        comment = commentSheet,
        onDismiss = {
            commentSheet = null
        },
    )
}

@Composable
internal fun CommentsContent(
    state: CommentsState,
    modifier: Modifier = Modifier,
    onClick: (Comment) -> Unit = {},
    onFilterClick: (CommentsFilter) -> Unit = {},
    onBackClick: () -> Unit = {},
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
            imageUrl = state.backgroundUrl,
            scrollState = listState,
        )

        ContentList(
            listState = listState,
            listItems = (state.items ?: emptyList()).toImmutableList(),
            listFilter = state.filter,
            contentPadding = contentPadding,
            loading = state.loading.isLoading,
            onClick = onClick,
            onFilterClick = onFilterClick,
            onBackClick = onBackClick,
        )
    }
}

@Composable
private fun ContentList(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues,
    listItems: ImmutableList<Comment>,
    listState: LazyListState,
    listFilter: CommentsFilter?,
    loading: Boolean,
    onClick: (Comment) -> Unit,
    onFilterClick: (CommentsFilter) -> Unit,
    onBackClick: () -> Unit,
) {
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

        item {
            ContentFilters(
                selectedFilter = listFilter,
                onClick = onFilterClick,
                modifier = Modifier
                    .padding(bottom = 20.dp),
            )
        }

        if (!loading && listItems.isNotEmpty()) {
            items(
                items = listItems,
                key = { it.id },
            ) { item ->
                ContentListItem(
                    item = item,
                    onClick = { onClick(item) },
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .animateItem(
                            fadeInSpec = null,
                            fadeOutSpec = null,
                        ),
                )
            }
        }

        if (loading) {
            items(3) {
                CommentSkeletonCard(
                    corner = 20.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(TraktTheme.size.commentCardSize)
                        .padding(bottom = 16.dp)
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
private fun ContentListItem(
    item: Comment,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    CommentCard(
        comment = item,
        maxLines = 20,
        corner = 20.dp,
        onClick = onClick,
        modifier = modifier,
    )
}

@Composable
private fun ContentFilters(
    selectedFilter: CommentsFilter?,
    onClick: (CommentsFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    FilterChipGroup(
        paddingVertical = PaddingValues.Zero,
        paddingHorizontal = PaddingValues.Zero,
        modifier = modifier,
    ) {
        for (filter in CommentsFilter.entries) {
            FilterChip(
                selected = selectedFilter == filter,
                text = stringResource(filter.displayRes),
                onClick = {
                    onClick(filter)
                },
            )
        }
    }
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
            text = stringResource(R.string.list_title_comments),
            color = TraktTheme.colors.textPrimary,
            style = TraktTheme.typography.heading5,
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
        CommentsContent(
            state = CommentsState(
                loading = DONE,
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
        CommentsContent(
            state = CommentsState(
                loading = LOADING,
            ),
        )
    }
}
