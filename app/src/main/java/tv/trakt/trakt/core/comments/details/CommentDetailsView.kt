@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.comments.details

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.W700
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.ColorImage
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.extensions.longDateTimeFormat
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.model.Comment
import tv.trakt.trakt.common.ui.theme.colors.Shade500
import tv.trakt.trakt.common.ui.theme.colors.Shade800
import tv.trakt.trakt.core.comments.ui.CommentCardSkeleton
import tv.trakt.trakt.core.comments.ui.CommentReplyCard
import tv.trakt.trakt.helpers.preview.PreviewData
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun CommentDetailsView(
    viewModel: CommentDetailsViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    CommentDetailsViewContent(
        state = state,
        modifier = modifier,
    )
}

@Composable
private fun CommentDetailsViewContent(
    state: CommentDetailsState,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        verticalArrangement = spacedBy(16.dp),
        overscrollEffect = null,
        modifier = modifier,
    ) {
        state.comment?.let { comment ->
            item {
                CommentContent(
                    comment = comment,
                    commentReplies = state.replies,
                )
            }
        }

        when {
            state.loading.isLoading &&
                (state.comment?.replies ?: 0) > 0 -> {
                item {
                    CommentCardSkeleton(
                        containerColor = TraktTheme.colors.commentReplyContainer,
                        shimmerColor = Shade800,
                        modifier = Modifier
                            .height(132.dp)
                            .fillMaxWidth(),
                    )
                }
            }
            state.loading == DONE && state.replies.isNullOrEmpty() -> {
                item {
                    Text(
                        text = stringResource(R.string.list_placeholder_replies),
                        color = TraktTheme.colors.textPrimary,
                        style = TraktTheme.typography.heading6,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .height(24.dp)
                            .wrapContentHeight()
                            .fillMaxWidth(),
                    )
                }
            }
        }

        item {
            Spacer(
                modifier = Modifier
                    .height(64.dp),
            )
        }
    }
}

@Composable
private fun CommentContent(
    comment: Comment,
    commentReplies: ImmutableList<Comment>?,
) {
    var isCollapsed by remember { mutableStateOf(true) }

    Column(
        verticalArrangement = spacedBy(0.dp),
    ) {
        CommentHeader(
            comment = comment,
            modifier = Modifier.padding(top = 5.dp),
        )

        Text(
            text = comment.commentNoSpoilers,
            style = TraktTheme.typography.paragraphSmall.copy(lineHeight = 1.3.em),
            color = TraktTheme.colors.textSecondary,
            overflow = if (isCollapsed) TextOverflow.Ellipsis else TextOverflow.Clip,
            maxLines = if (isCollapsed) 12 else Int.MAX_VALUE,
            modifier = Modifier
                .onClick {
                    isCollapsed = !isCollapsed
                }
                .padding(top = 16.dp),
        )

        Crossfade(
            targetState = commentReplies,
            animationSpec = tween(200),
        ) { replies ->
            if (!replies.isNullOrEmpty()) {
                Column(
                    verticalArrangement = spacedBy(16.dp),
                    modifier = Modifier
                        .padding(top = 20.dp),
                ) {
                    for (reply in replies) {
                        CommentReplyCard(
                            comment = reply,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CommentHeader(
    comment: Comment,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Absolute.spacedBy(12.dp),
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier.size(36.dp),
        ) {
            val avatarBorder = when {
                comment.user.isAnyVip -> Color.Red
                else -> Color.Transparent
            }
            val avatar = comment.user.images?.avatar?.full
            if (avatar != null) {
                AsyncImage(
                    model = avatar,
                    contentDescription = "User avatar",
                    contentScale = ContentScale.Crop,
                    error = painterResource(R.drawable.ic_person_placeholder),
                    modifier = Modifier
                        .border(2.dp, avatarBorder, CircleShape)
                        .clip(CircleShape),
                )
            } else {
                Image(
                    painter = painterResource(R.drawable.ic_person_placeholder),
                    contentDescription = null,
                    modifier = Modifier
                        .border(2.dp, avatarBorder, CircleShape)
                        .clip(CircleShape),
                )
            }

            comment.userLiteRating?.let {
                Icon(
                    painter = painterResource(it.iconRes),
                    contentDescription = it.name,
                    tint = it.tint,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .graphicsLayer {
                            translationX = 4.dp.toPx()
                            translationY = -4.dp.toPx()
                        }
                        .background(Shade500, shape = CircleShape)
                        .size(18.dp)
                        .padding(3.dp),
                )
            }
        }

        Column(verticalArrangement = Arrangement.Absolute.spacedBy(2.dp)) {
            Row(
                horizontalArrangement = Arrangement.Absolute.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(
                        if (comment.isReview) {
                            R.string.text_review_by
                        } else {
                            R.string.text_shout_by
                        },
                    ),
                    style = TraktTheme.typography.paragraph,
                    color = TraktTheme.colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = comment.user.displayName,
                    style = TraktTheme.typography.paragraph.copy(fontWeight = FontWeight.W600),
                    color = TraktTheme.colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text = comment.createdAt.format(longDateTimeFormat),
                style = TraktTheme.typography.meta,
                color = TraktTheme.colors.textSecondary
                    .copy(alpha = 0.66f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun CommentFooter(
    comment: Comment,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Absolute.spacedBy(16.dp, Alignment.End),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.Absolute.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_comment),
                contentDescription = "Replies",
                tint = TraktTheme.colors.textPrimary,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = "${comment.replies}",
                style = TraktTheme.typography.paragraphSmall.copy(fontWeight = W700),
                color = TraktTheme.colors.textPrimary,
            )
        }
    }
}

@OptIn(ExperimentalCoilApi::class)
@Preview(
    showBackground = true,
    backgroundColor = 0xFF212427,
)
@Composable
private fun Preview() {
    TraktTheme {
        val previewHandler = AsyncImagePreviewHandler {
            ColorImage(Color.Blue.toArgb())
        }
        CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
            Column(
                verticalArrangement = spacedBy(48.dp),
            ) {
                CommentDetailsViewContent(
                    state = CommentDetailsState(
                        comment = PreviewData.comment1,
                        loading = LoadingState.LOADING,
                    ),
                )

                CommentDetailsViewContent(
                    state = CommentDetailsState(
                        comment = PreviewData.comment1,
                        replies = listOf(
                            PreviewData.comment1,
                        ).toImmutableList(),
                        loading = DONE,
                    ),
                )
            }
        }
    }
}
