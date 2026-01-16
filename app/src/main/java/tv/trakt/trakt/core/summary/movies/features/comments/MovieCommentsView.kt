@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package tv.trakt.trakt.core.summary.movies.features.comments

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.ColorImage
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.LoadingState.IDLE
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.Comment
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.reactions.Reaction
import tv.trakt.trakt.common.model.reactions.ReactionsSummary
import tv.trakt.trakt.common.model.toTraktId
import tv.trakt.trakt.core.comments.features.deletecomment.DeleteCommentSheet
import tv.trakt.trakt.core.comments.features.details.CommentDetailsSheet
import tv.trakt.trakt.core.comments.features.postcomment.PostCommentSheet
import tv.trakt.trakt.core.comments.model.CommentsFilter
import tv.trakt.trakt.core.comments.ui.CommentCard
import tv.trakt.trakt.core.comments.ui.CommentSkeletonCard
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.FilterChip
import tv.trakt.trakt.ui.components.FilterChipGroup
import tv.trakt.trakt.ui.components.TraktSectionHeader
import tv.trakt.trakt.ui.theme.HorizontalImageAspectRatio
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun MovieCommentsView(
    viewModel: MovieCommentsViewModel,
    headerPadding: PaddingValues,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    onMoreClick: ((CommentsFilter) -> Unit)?,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    var commentSheet by remember { mutableStateOf<Comment?>(null) }
    var postCommentSheet by remember { mutableStateOf(false) }
    var deleteCommentSheet by remember { mutableStateOf<Comment?>(null) }

    MovieCommentsContent(
        state = state,
        modifier = modifier,
        headerPadding = headerPadding,
        contentPadding = contentPadding,
        onMoreClick = {
            if (!state.items.isNullOrEmpty()) {
                onMoreClick?.invoke(state.filter)
            }
        },
        onCommentLoaded = {
            viewModel.loadReactions(it.id)
        },
        onCommentClick = {
            commentSheet = it
        },
        onAddCommentClick = {
            postCommentSheet = true
        },
        onDeleteCommentClick = {
            deleteCommentSheet = it
        },
        onFilterClick = {
            viewModel.setFilter(it)
        },
        onReactionClick = { reaction, comment ->
            viewModel.setReaction(reaction, comment.id)
        },
        onCollapse = viewModel::setCollapsed,
    )

    CommentDetailsSheet(
        comment = commentSheet,
        onDeleteComment = viewModel::deleteComment,
        onDismiss = {
            commentSheet = null
        },
    )

    PostCommentSheet(
        active = postCommentSheet,
        mediaId = state.movie?.ids?.trakt,
        mediaType = MediaType.MOVIE,
        onCommentPost = viewModel::addComment,
        onDismiss = {
            postCommentSheet = false
        },
    )

    DeleteCommentSheet(
        active = deleteCommentSheet != null,
        commentId = deleteCommentSheet?.id?.toTraktId(),
        onDeleted = viewModel::deleteComment,
        onDismiss = {
            deleteCommentSheet = null
        },
    )
}

@Composable
private fun MovieCommentsContent(
    state: MovieCommentsState,
    modifier: Modifier = Modifier,
    headerPadding: PaddingValues = PaddingValues(),
    contentPadding: PaddingValues = PaddingValues(),
    onCommentLoaded: ((Comment) -> Unit)? = null,
    onCommentClick: ((Comment) -> Unit)? = null,
    onReactionClick: ((Reaction, Comment) -> Unit)? = null,
    onFilterClick: ((CommentsFilter) -> Unit)? = null,
    onAddCommentClick: (() -> Unit)? = null,
    onDeleteCommentClick: ((Comment) -> Unit)? = null,
    onMoreClick: (() -> Unit)? = null,
    onCollapse: ((Boolean) -> Unit)? = null,
) {
    var animateCollapse by rememberSaveable { mutableStateOf(false) }

    Column(
        verticalArrangement = spacedBy(TraktTheme.spacing.mainRowHeaderSpace),
        modifier = modifier
            .animateContentSize(animationSpec = if (animateCollapse) spring() else snap()),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(headerPadding),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = CenterVertically,
        ) {
            TraktSectionHeader(
                title = stringResource(R.string.list_title_comments),
                chevron = !state.items.isNullOrEmpty() || state.loading != DONE,
                collapsed = state.collapsed ?: false,
                onCollapseClick = {
                    animateCollapse = true
                    val current = (state.collapsed ?: false)
                    onCollapse?.invoke(!current)
                },
                extraIcon = when {
                    state.user != null -> {
                        {
                            Icon(
                                painter = painterResource(R.drawable.ic_comment_plus),
                                contentDescription = null,
                                tint = TraktTheme.colors.textPrimary,
                                modifier = Modifier
                                    .padding(
                                        start = 12.dp,
                                        end = 6.dp,
                                    )
                                    .size(18.dp)
                                    .onClick(enabled = state.loading == DONE) {
                                        onAddCommentClick?.invoke()
                                    }
                                    .graphicsLayer {
                                        translationY = 0.75.dp.toPx()
                                    },
                            )
                        }
                    }
                    else -> {
                        null
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .onClick(enabled = state.loading == DONE) {
                        onMoreClick?.invoke()
                    },
            )
        }

        if (state.collapsed != true) {
            if (!state.items.isNullOrEmpty() || state.loading.isLoading || state.user != null) {
                ContentFilters(
                    selectedFilter = state.filter,
                    headerPadding = headerPadding,
                    onFilterClick = onFilterClick,
                )
            } else {
                Spacer(modifier = Modifier.height(TraktTheme.spacing.mainRowHeaderSpace))
            }

            Crossfade(
                targetState = state.loading,
                animationSpec = tween(200),
            ) { loading ->
                when (loading) {
                    IDLE, LOADING -> {
                        ContentLoading(
                            visible = loading.isLoading,
                            contentPadding = contentPadding,
                        )
                    }

                    DONE -> {
                        Column(
                            verticalArrangement = spacedBy(0.dp),
                        ) {
                            if (state.items?.isEmpty() == true) {
                                ContentEmpty(
                                    contentPadding = headerPadding,
                                )
                            } else {
                                ContentList(
                                    listItems = (state.items ?: emptyList()).toImmutableList(),
                                    listReactions = (state.reactions ?: emptyMap()).toImmutableMap(),
                                    user = state.user,
                                    userReactions = (state.userReactions ?: emptyMap()).toImmutableMap(),
                                    contentPadding = contentPadding,
                                    onCommentClick = onCommentClick,
                                    onDeleteCommentClick = onDeleteCommentClick,
                                    onCommentLoaded = onCommentLoaded,
                                    onReactionClick = onReactionClick,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ContentList(
    listItems: ImmutableList<Comment>,
    listReactions: ImmutableMap<Int, ReactionsSummary>,
    listState: LazyListState = rememberLazyListState(),
    user: User?,
    userReactions: ImmutableMap<Int, Reaction?>,
    contentPadding: PaddingValues,
    onCommentLoaded: ((Comment) -> Unit)? = null,
    onCommentClick: ((Comment) -> Unit)? = null,
    onDeleteCommentClick: ((Comment) -> Unit)? = null,
    onReactionClick: ((Reaction, Comment) -> Unit)? = null,
) {
    val currentList = remember { mutableIntStateOf(listItems.hashCode()) }

    LaunchedEffect(listItems) {
        val hashCode = listItems.hashCode()
        if (currentList.intValue != hashCode) {
            currentList.intValue = hashCode
            listState.animateScrollToItem(0)
        }
    }

    LazyRow(
        state = listState,
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = spacedBy(TraktTheme.spacing.mainRowSpace),
        contentPadding = contentPadding,
    ) {
        items(
            items = listItems,
            key = { it.id },
        ) { comment ->
            CommentCard(
                user = user,
                comment = comment,
                reactions = listReactions,
                userReactions = userReactions,
                deleteEnabled = false,
                onClick = { onCommentClick?.invoke(comment) },
                onRepliesClick = { onCommentClick?.invoke(comment) },
                onDeleteClick = { onDeleteCommentClick?.invoke(comment) },
                onRequestReactions = { onCommentLoaded?.invoke(comment) },
                onReactionClick = onReactionClick,
                modifier = Modifier
                    .height(TraktTheme.size.commentCardSize)
                    .aspectRatio(HorizontalImageAspectRatio)
                    .animateItem(
                        fadeInSpec = null,
                        fadeOutSpec = null,
                    ),
            )
        }
    }
}

@Composable
private fun ContentFilters(
    headerPadding: PaddingValues,
    selectedFilter: CommentsFilter,
    onFilterClick: ((CommentsFilter) -> Unit)? = null,
) {
    FilterChipGroup(
        paddingHorizontal = headerPadding,
        paddingVertical = PaddingValues(
            bottom = 3.dp,
        ),
    ) {
        for (filter in CommentsFilter.entries) {
            FilterChip(
                selected = selectedFilter == filter,
                text = stringResource(filter.displayRes),
                leadingContent = {
                    Icon(
                        painter = painterResource(filter.iconRes),
                        contentDescription = null,
                        tint = TraktTheme.colors.textPrimary,
                        modifier = Modifier
                            .size(FilterChipDefaults.IconSize)
                            .graphicsLayer {
                                translationX = (-1).dp.toPx()
                            },
                    )
                },
                onClick = { onFilterClick?.invoke(filter) },
            )
        }
    }
}

@Composable
private fun ContentLoading(
    visible: Boolean = true,
    contentPadding: PaddingValues,
) {
    LazyRow(
        horizontalArrangement = spacedBy(TraktTheme.spacing.mainRowSpace),
        contentPadding = contentPadding,
        userScrollEnabled = false,
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (visible) 1F else 0F),
    ) {
        items(count = 3) {
            CommentSkeletonCard(
                modifier = Modifier
                    .height(TraktTheme.size.commentCardSize)
                    .aspectRatio(HorizontalImageAspectRatio),
            )
        }
    }
}

@Composable
private fun ContentEmpty(contentPadding: PaddingValues) {
    Text(
        text = stringResource(R.string.list_placeholder_comments),
        color = TraktTheme.colors.textSecondary,
        style = TraktTheme.typography.heading6,
        modifier = Modifier.padding(contentPadding),
    )
}

@OptIn(ExperimentalCoilApi::class)
@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview() {
    TraktTheme {
        val previewHandler = AsyncImagePreviewHandler {
            ColorImage(Color.Blue.toArgb())
        }
        CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
            MovieCommentsContent(
                state = MovieCommentsState(),
            )
        }
    }
}

@OptIn(ExperimentalCoilApi::class)
@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview2() {
    TraktTheme {
        val previewHandler = AsyncImagePreviewHandler {
            ColorImage(Color.Blue.toArgb())
        }
        CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
            MovieCommentsContent(
                state = MovieCommentsState(
                    user = PreviewData.user1,
                    items = emptyList<Comment>().toImmutableList(),
                    loading = LOADING,
                ),
            )
        }
    }
}

@OptIn(ExperimentalCoilApi::class)
@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview3() {
    TraktTheme {
        val previewHandler = AsyncImagePreviewHandler {
            ColorImage(Color.Blue.toArgb())
        }
        CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
            MovieCommentsContent(
                state = MovieCommentsState(
                    items = emptyList<Comment>().toImmutableList(),
                    loading = DONE,
                ),
            )
        }
    }
}
