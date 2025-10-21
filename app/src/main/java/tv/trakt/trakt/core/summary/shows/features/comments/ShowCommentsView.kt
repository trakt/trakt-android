@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package tv.trakt.trakt.core.summary.shows.features.comments

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import tv.trakt.trakt.common.model.Comment
import tv.trakt.trakt.common.model.reactions.Reaction
import tv.trakt.trakt.common.model.reactions.ReactionsSummary
import tv.trakt.trakt.core.comments.details.CommentDetailsSheet
import tv.trakt.trakt.core.comments.ui.CommentCard
import tv.trakt.trakt.core.comments.ui.CommentSkeletonCard
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.theme.HorizontalImageAspectRatio
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun ShowCommentsView(
    viewModel: ShowCommentsViewModel,
    headerPadding: PaddingValues,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    onMoreClick: (() -> Unit)?,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    var commentSheet by remember { mutableStateOf<Comment?>(null) }

    ShowCommentsContent(
        state = state,
        modifier = modifier,
        headerPadding = headerPadding,
        contentPadding = contentPadding,
        onMoreClick = onMoreClick,
        onCommentLoaded = {
            viewModel.loadReactions(it.id)
        },
        onCommentClick = {
            commentSheet = it
        },
        onReactionClick = { reaction, comment ->
            viewModel.setReaction(reaction, comment.id)
        },
    )

    CommentDetailsSheet(
        comment = commentSheet,
        onDismiss = {
            commentSheet = null
        },
    )
}

@Composable
private fun ShowCommentsContent(
    state: ShowCommentsState,
    modifier: Modifier = Modifier,
    headerPadding: PaddingValues = PaddingValues(),
    contentPadding: PaddingValues = PaddingValues(),
    onCommentLoaded: ((Comment) -> Unit)? = null,
    onCommentClick: ((Comment) -> Unit)? = null,
    onReactionClick: ((Reaction, Comment) -> Unit)? = null,
    onMoreClick: (() -> Unit)? = null,
) {
    Column(
        verticalArrangement = spacedBy(TraktTheme.spacing.mainRowHeaderSpace),
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(headerPadding)
                .onClick(enabled = state.loading == DONE) {
                    onMoreClick?.invoke()
                },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = CenterVertically,
        ) {
            TraktHeader(
                title = stringResource(R.string.list_title_comments),
            )

            if (state.loading.isLoading || !state.items.isNullOrEmpty()) {
                Icon(
                    painter = painterResource(R.drawable.ic_chevron_right),
                    contentDescription = null,
                    tint = TraktTheme.colors.textPrimary,
                    modifier = Modifier
                        .size(20.dp)
                        .graphicsLayer {
                            translationX = (4.9).dp.toPx()
                        },
                )
            }
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
                                listReactions = state.reactions,
                                userReactions = (state.userReactions ?: emptyMap()).toImmutableMap(),
                                reactionsEnabled = state.user != null,
                                contentPadding = contentPadding,
                                onCommentClick = onCommentClick,
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

@Composable
private fun ContentList(
    listItems: ImmutableList<Comment>,
    listReactions: ImmutableMap<Int, ReactionsSummary>?,
    listState: LazyListState = rememberLazyListState(),
    reactionsEnabled: Boolean,
    userReactions: ImmutableMap<Int, Reaction?>,
    contentPadding: PaddingValues,
    onCommentLoaded: ((Comment) -> Unit)? = null,
    onCommentClick: ((Comment) -> Unit)? = null,
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
                comment = comment,
                reactions = listReactions?.get(comment.id),
                userReaction = userReactions[comment.id],
                onClick = { onCommentClick?.invoke(comment) },
                onRequestReactions = { onCommentLoaded?.invoke(comment) },
                reactionsEnabled = reactionsEnabled,
                onReactionClick = { onReactionClick?.invoke(it, comment) },
                modifier = Modifier
                    .height(TraktTheme.size.commentCardSize)
                    .aspectRatio(HorizontalImageAspectRatio),
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
            ShowCommentsContent(
                state = ShowCommentsState(),
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
            ShowCommentsContent(
                state = ShowCommentsState(
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
            ShowCommentsContent(
                state = ShowCommentsState(
                    items = emptyList<Comment>().toImmutableList(),
                    loading = DONE,
                ),
            )
        }
    }
}
