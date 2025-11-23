@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package tv.trakt.trakt.core.comments

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.model.Comment
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.reactions.Reaction
import tv.trakt.trakt.common.model.reactions.ReactionsSummary
import tv.trakt.trakt.core.comments.features.details.CommentDetailsSheet
import tv.trakt.trakt.core.comments.features.postcomment.PostCommentSheet
import tv.trakt.trakt.core.comments.model.CommentsFilter
import tv.trakt.trakt.core.comments.ui.CommentCard
import tv.trakt.trakt.core.comments.ui.CommentSkeletonCard
import tv.trakt.trakt.helpers.SimpleScrollConnection
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.FilterChip
import tv.trakt.trakt.ui.components.FilterChipGroup
import tv.trakt.trakt.ui.components.ScrollableBackdropImage
import tv.trakt.trakt.ui.components.buttons.TertiaryButton
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun CommentsScreen(
    modifier: Modifier = Modifier,
    viewModel: CommentsViewModel,
    onNavigateBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    var commentSheet by remember { mutableStateOf<Comment?>(null) }
    var postCommentSheet by remember { mutableStateOf(false) }

    CommentsContent(
        state = state,
        modifier = modifier,
        onCommentLoaded = {
            viewModel.loadReactions(it.id)
        },
        onCommentClick = {
            commentSheet = it
        },
        onFilterClick = {
            viewModel.setFilter(it)
        },
        onReactionClick = { reaction, comment ->
            viewModel.setReaction(reaction, comment.id)
        },
        onNewCommentClick = {
            postCommentSheet = true
        },
        onBackClick = onNavigateBack,
    )

    CommentDetailsSheet(
        comment = commentSheet,
        onDismiss = {
            commentSheet = null
        },
    )

    PostCommentSheet(
        active = postCommentSheet,
        mediaId = state.media?.id,
        mediaType = state.media?.type,
        onCommentPost = viewModel::addComment,
        onDismiss = {
            postCommentSheet = false
        },
    )
}

@Composable
internal fun CommentsContent(
    state: CommentsState,
    modifier: Modifier = Modifier,
    onCommentLoaded: ((Comment) -> Unit)? = null,
    onCommentClick: ((Comment) -> Unit)? = null,
    onFilterClick: ((CommentsFilter) -> Unit)? = null,
    onReactionClick: ((Reaction, Comment) -> Unit)? = null,
    onNewCommentClick: (() -> Unit)? = null,
    onBackClick: (() -> Unit)? = null,
) {
    val listState = rememberLazyListState(
        cacheWindow = LazyLayoutCacheWindow(
            aheadFraction = 0.5F,
            behindFraction = 0.5F,
        ),
    )

    val listScrollConnection = rememberSaveable(saver = SimpleScrollConnection.Saver) {
        SimpleScrollConnection()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TraktTheme.colors.backgroundPrimary)
            .nestedScroll(listScrollConnection),
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
            translation = listScrollConnection.resultOffset,
        )

        ContentList(
            listState = listState,
            listItems = (state.items ?: emptyList()).toImmutableList(),
            listFilter = state.filter,
            listReactions = state.reactions,
            userReactions = (state.userReactions ?: emptyMap()).toImmutableMap(),
            contentPadding = contentPadding,
            loading = state.loading.isLoading,
            user = state.user,
            onCommentLoaded = onCommentLoaded,
            onCommentClick = onCommentClick,
            onFilterClick = onFilterClick,
            onReactionClick = onReactionClick,
            onBackClick = onBackClick,
        )

        AnimatedVisibility(
            visible = state.user != null && !state.loading.isLoading,
            enter = fadeIn(tween(delayMillis = 300)),
            exit = fadeOut(tween(delayMillis = 300)),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(
                    end = TraktTheme.spacing.mainPageHorizontalSpace + 10.dp,
                    bottom = WindowInsets.navigationBars.asPaddingValues()
                        .calculateBottomPadding()
                        .plus(TraktTheme.size.navigationBarHeight)
                        .plus(10.dp),
                ),
        ) {
            TertiaryButton(
                text = stringResource(R.string.button_text_comment),
                icon = painterResource(R.drawable.ic_comment_plus),
                height = 38.dp,
                onClick = onNewCommentClick ?: {},
                modifier = Modifier
//                    .shadow(2.5.dp, RoundedCornerShape(12.dp))
                    .dropShadow(
                        shape = RoundedCornerShape(12.dp),
                        shadow = Shadow(
                            radius = 4.dp,
                            color = Color.Black,
                            spread = 2.dp,
                            alpha = 0.2f,
                        ),
                    ),
            )
        }
    }
}

@Composable
private fun ContentList(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues,
    listItems: ImmutableList<Comment>,
    listReactions: ImmutableMap<Int, ReactionsSummary>?,
    listState: LazyListState,
    listFilter: CommentsFilter?,
    loading: Boolean,
    user: User?,
    userReactions: ImmutableMap<Int, Reaction?>,
    onCommentLoaded: ((Comment) -> Unit)? = null,
    onCommentClick: ((Comment) -> Unit)? = null,
    onFilterClick: ((CommentsFilter) -> Unit)? = null,
    onReactionClick: ((Reaction, Comment) -> Unit)? = null,
    onBackClick: (() -> Unit)? = null,
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
                    .onClick {
                        onBackClick?.invoke()
                    },
            )
        }

        item {
            ContentFilters(
                selectedFilter = listFilter,
                onClick = onFilterClick ?: {},
                modifier = Modifier
                    .padding(bottom = 20.dp),
            )
        }

        if (!loading && listItems.isNotEmpty()) {
            items(
                items = listItems,
                key = { it.id },
            ) { comment ->
                val isUserComment = remember(user) {
                    comment.user.ids.trakt == user?.ids?.trakt
                }

                CommentCard(
                    comment = comment,
                    reactions = listReactions?.get(comment.id),
                    userReaction = userReactions[comment.id],
                    onRequestReactions = { onCommentLoaded?.invoke(comment) },
                    reactionsEnabled = user != null,
                    userComment = isUserComment,
                    onReactionClick = { onReactionClick?.invoke(it, comment) },
                    maxLines = Int.MAX_VALUE,
                    corner = 20.dp,
                    onClick = { onCommentClick?.invoke(comment) },
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
                leadingContent = {
                    Icon(
                        painter = painterResource(filter.iconRes),
                        contentDescription = null,
                        tint = TraktTheme.colors.textPrimary,
                        modifier = Modifier.size(FilterChipDefaults.IconSize),
                    )
                },
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
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = CenterVertically,
            horizontalArrangement = spacedBy(12.dp),
            modifier = Modifier
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
