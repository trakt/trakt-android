package tv.trakt.trakt.app.core.details.comments

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import coil3.ColorImage
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import kotlinx.collections.immutable.ImmutableList
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.app.common.ui.TvVipChip
import tv.trakt.trakt.app.core.comments.ui.CommentReplyCard
import tv.trakt.trakt.app.helpers.preview.PreviewData
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.common.helpers.extensions.longDateTimeFormat
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.model.Comment
import tv.trakt.trakt.common.ui.composables.FilmProgressIndicator
import tv.trakt.trakt.common.ui.theme.colors.Shade500
import tv.trakt.trakt.resources.R

@Composable
internal fun CommentDetailsDialog(
    comment: Comment,
    modifier: Modifier = Modifier,
    viewModel: CommentDetailsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(comment.id) {
        viewModel.loadCommentReplies(comment.id)
    }

    CommentDetailsContent(
        modifier = modifier,
        comment = comment,
        state = state,
    )
}

@Composable
private fun CommentDetailsContent(
    modifier: Modifier,
    comment: Comment,
    state: CommentDetailsState,
) {
    val focusRequester = remember { FocusRequester() }

    var isExpanded by remember { mutableStateOf(false) }
    var isFocused by remember { mutableStateOf(false) }
    val accentColor = TraktTheme.colors.accent

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = spacedBy(0.dp),
        modifier = modifier
            .fillMaxSize()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(24.dp),
            )
            .clip(RoundedCornerShape(24.dp))
            .verticalScroll(rememberScrollState())
            .background(TraktTheme.colors.commentContainer)
            .padding(
                vertical = 20.dp,
                horizontal = 20.dp,
            ),
    ) {
        Text(
            text = "Replies",
            color = TraktTheme.colors.textPrimary,
            style = TraktTheme.typography.heading4,
        )

        CommentHeader(
            comment = comment,
            modifier = Modifier
                .padding(top = 20.dp)
                .focusRequester(focusRequester)
                .focusable(),
        )

        Text(
            text = comment.commentNoSpoilers,
            style = TraktTheme.typography.paragraphSmall,
            color = TraktTheme.colors.textSecondary,
            maxLines = if (isExpanded) Int.MAX_VALUE else 3,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .padding(top = 16.dp)
                .onFocusChanged {
                    isFocused = it.isFocused
                }
                .onClick {
                    isExpanded = !isExpanded
                }
                .drawWithContent {
                    drawContent()
                    if (isFocused) {
                        val offset = 10.dp
                        drawRoundRect(
                            color = accentColor,
                            topLeft = Offset(-offset.toPx(), -(offset / 1.5F).toPx()),
                            size = Size(
                                width = size.width + (offset * 2).toPx(),
                                height = size.height + (offset * 1.5F).toPx(),
                            ),
                            cornerRadius = CornerRadius(16.dp.toPx()),
                            style = Stroke(width = 2.5.dp.toPx()),
                        )
                    }
                },
        )

        if (state.isLoading) {
            FilmProgressIndicator(
                color = TraktTheme.colors.progressPrimary,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 32.dp),
            )
        }

        if (!state.isLoading && state.commentReplies?.isNotEmpty() == true) {
            CommentRepliesContent(
                comments = state.commentReplies,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
            )
        }

        if (!state.isLoading && state.commentReplies?.isEmpty() == true) {
            Text(
                text = stringResource(R.string.list_placeholder_comments),
                style = TraktTheme.typography.paragraphSmall,
                color = TraktTheme.colors.textSecondary,
                textAlign = TextAlign.Center,
                maxLines = 1,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp),
            )
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
        horizontalArrangement = spacedBy(8.dp),
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp),
        ) {
            val avatarBorder = if (comment.user.isAnyVip) Color.Red else Color.Transparent
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

        Column(verticalArrangement = spacedBy(3.dp)) {
            Row(
                horizontalArrangement = spacedBy(4.dp),
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
                if (comment.user.isAnyVip) {
                    TvVipChip()
                }
            }
            Text(
                text = comment.createdAt.format(longDateTimeFormat),
                style = TraktTheme.typography.paragraphSmall,
                color = TraktTheme.colors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun CommentRepliesContent(
    comments: ImmutableList<Comment>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = spacedBy(12.dp),
    ) {
        comments.forEach { comment ->
            CommentReplyCard(
                comment = comment,
            )
        }
    }
}

@OptIn(ExperimentalCoilApi::class)
@Preview(heightDp = 600, widthDp = 400)
@Composable
fun CommentDetailsPreview() {
    TraktTheme {
        val previewHandler = AsyncImagePreviewHandler {
            ColorImage(Color.LightGray.toArgb())
        }
        CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
            Column(
                verticalArrangement = spacedBy(32.dp),
            ) {
                CommentDetailsContent(
                    comment = PreviewData.comment1,
                    modifier = Modifier,
                    state = CommentDetailsState(
                        isLoading = false,
                        commentReplies = null,
                    ),
                )
            }
        }
    }
}
