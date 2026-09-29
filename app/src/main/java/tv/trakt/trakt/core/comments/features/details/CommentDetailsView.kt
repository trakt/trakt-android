@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.comments.features.details

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.W700
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
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.launch
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.extensions.capitalize
import tv.trakt.trakt.common.helpers.extensions.googleTranslateActivityInfo
import tv.trakt.trakt.common.helpers.extensions.longDateTimeFormat
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.extensions.openGoogleTranslate
import tv.trakt.trakt.common.helpers.extensions.toLocal
import tv.trakt.trakt.common.helpers.extensions.toMarkdownText
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.Comment
import tv.trakt.trakt.common.model.CommentGif
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.reactions.Reaction
import tv.trakt.trakt.common.model.reactions.ReactionsSummary
import tv.trakt.trakt.common.model.toTraktId
import tv.trakt.trakt.common.ui.theme.colors.Shade800
import tv.trakt.trakt.core.comments.features.deletecomment.DeleteCommentSheet
import tv.trakt.trakt.core.comments.features.editcomment.EditCommentSheet
import tv.trakt.trakt.core.comments.features.postreply.PostReplySheet
import tv.trakt.trakt.core.comments.model.MentionSource
import tv.trakt.trakt.core.comments.features.report.ReportCommentSheet
import tv.trakt.trakt.core.comments.ui.CommentDropdown
import tv.trakt.trakt.core.comments.ui.CommentGifView
import tv.trakt.trakt.core.comments.ui.CommentReplyCard
import tv.trakt.trakt.core.comments.ui.CommentSkeletonCard
import tv.trakt.trakt.core.comments.ui.CommentUserChips
import tv.trakt.trakt.core.reactions.ui.ReactionsSummaryChip
import tv.trakt.trakt.core.reactions.ui.ReactionsToolTip
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun CommentDetailsView(
    viewModel: CommentDetailsViewModel,
    modifier: Modifier = Modifier,
    gifQuery: String? = null,
    mentionSource: MentionSource? = null,
    progressEnabled: Boolean = false,
    onDeleteComment: (commentId: TraktId) -> Unit = {},
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    var postReplySheet by remember { mutableStateOf<User?>(null) }
    var editCommentSheet by remember { mutableStateOf<Comment?>(null) }
    var deleteCommentSheet by remember { mutableStateOf<Comment?>(null) }
    var deleteReplySheet by remember { mutableStateOf<Comment?>(null) }
    var reportCommentSheet by remember { mutableStateOf<Comment?>(null) }

    CommentDetailsViewContent(
        state = state,
        progressEnabled = progressEnabled,
        modifier = modifier,
        onReplyLoaded = {
            viewModel.loadReactions(it.id)
        },
        onReactionClick = { reaction, comment ->
            viewModel.setReaction(reaction, comment.id)
        },
        onReplyClick = { user ->
            postReplySheet = user
        },
        onEditClick = { comment ->
            editCommentSheet = comment
        },
        onDeleteClick = { comment ->
            deleteCommentSheet = comment
        },
        onDeleteReplyClick = { reply ->
            deleteReplySheet = reply
        },
        onReportClick = { comment ->
            reportCommentSheet = comment
        },
    )

    PostReplySheet(
        active = postReplySheet != null,
        comment = state.comment,
        user = postReplySheet,
        gifQuery = gifQuery,
        mentionSource = mentionSource,
        onReplyPost = viewModel::addReply,
        onDismiss = {
            postReplySheet = null
        },
    )

    EditCommentSheet(
        comment = editCommentSheet,
        gifQuery = gifQuery,
        onCommentEdit = viewModel::updateComment,
        onDismiss = {
            editCommentSheet = null
        },
    )

    DeleteCommentSheet(
        active = deleteCommentSheet != null,
        commentId = deleteCommentSheet?.id?.toTraktId(),
        skipSnack = true,
        onDeleted = onDeleteComment,
        onDismiss = {
            deleteCommentSheet = null
        },
    )

    DeleteCommentSheet(
        active = deleteReplySheet != null,
        commentId = deleteReplySheet?.id?.toTraktId(),
        isReply = true,
        skipSnack = true,
        onDeleted = {
            deleteReplySheet?.let {
                viewModel.deleteReply(replyId = it.id)
            }
        },
        onDismiss = {
            deleteReplySheet = null
        },
    )

    ReportCommentSheet(
        active = reportCommentSheet != null,
        comment = reportCommentSheet,
        onDismiss = {
            reportCommentSheet = null
        },
    )
}

@Composable
private fun CommentDetailsViewContent(
    state: CommentDetailsState,
    modifier: Modifier = Modifier,
    progressEnabled: Boolean = false,
    onReplyLoaded: ((Comment) -> Unit)? = null,
    onReactionClick: ((Reaction, Comment) -> Unit)? = null,
    onEditClick: ((Comment) -> Unit)? = null,
    onDeleteClick: ((Comment) -> Unit)? = null,
    onReplyClick: ((User) -> Unit)? = null,
    onDeleteReplyClick: ((Comment) -> Unit)? = null,
    onReportClick: ((Comment) -> Unit)? = null,
) {
    LazyColumn(
        verticalArrangement = spacedBy(16.dp),
        overscrollEffect = null,
        modifier = modifier,
    ) {
        state.comment?.let { comment ->
            item {
                CommentContent(
                    user = state.user,
                    comment = comment,
                    commentReplies = state.replies,
                    listReactions = state.reactions,
                    userReactions = (state.userReactions ?: emptyMap()).toImmutableMap(),
                    progressEnabled = progressEnabled,
                    onReplyLoaded = onReplyLoaded,
                    onReactionClick = onReactionClick,
                    onReplyClick = onReplyClick,
                    onEditClick = { onEditClick?.invoke(comment) },
                    onDeleteClick = { onDeleteClick?.invoke(comment) },
                    onDeleteReplyClick = onDeleteReplyClick,
                    onReportClick = { onReportClick?.invoke(comment) },
                )
            }
        }

        if (state.loading.isLoading && (state.comment?.replies ?: 0) > 0) {
            item {
                CommentSkeletonCard(
                    containerColor = TraktTheme.colors.commentReplyContainer,
                    shimmerColor = Shade800,
                    modifier = Modifier
                        .height(132.dp)
                        .fillMaxWidth(),
                )
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
    user: User?,
    comment: Comment,
    commentReplies: ImmutableList<Comment>?,
    listReactions: ImmutableMap<Int, ReactionsSummary>?,
    userReactions: ImmutableMap<Int, Reaction?>,
    progressEnabled: Boolean,
    onReplyLoaded: ((Comment) -> Unit)? = null,
    onReactionClick: ((Reaction, Comment) -> Unit)? = null,
    onReplyClick: ((User) -> Unit)? = null,
    onEditClick: (() -> Unit)? = null,
    onDeleteClick: (() -> Unit)? = null,
    onDeleteReplyClick: ((Comment) -> Unit)? = null,
    onReportClick: (() -> Unit)? = null,
) {
    var isCollapsed by remember { mutableStateOf(true) }

    Column(
        verticalArrangement = spacedBy(0.dp),
    ) {
        CommentHeader(
            comment = comment,
            userComment = user?.ids?.trakt == comment.user.ids.trakt,
            progressEnabled = progressEnabled,
            onEditClick = onEditClick,
            onDeleteClick = onDeleteClick,
            onReportClick = onReportClick,
            modifier = Modifier.padding(top = 5.dp),
        )

        val linkColor = TraktTheme.colors.textPrimary
        val markdownText = remember(comment.commentNoSpoilers, linkColor) {
            comment.commentNoSpoilers.toMarkdownText(linkColor)
        }

        if (comment.commentNoSpoilers.isNotBlank()) {
            SelectionContainer {
                Text(
                    text = markdownText,
                    style = TraktTheme.typography.paragraphSmall.copy(lineHeight = 1.3.em),
                    color = TraktTheme.colors.textSecondary,
                    overflow = if (isCollapsed) TextOverflow.Ellipsis else TextOverflow.Clip,
                    maxLines = if (isCollapsed) 20 else Int.MAX_VALUE,
                    modifier = Modifier
                        .onClick {
                            isCollapsed = !isCollapsed
                        }
                        .padding(top = 16.dp),
                )
            }
        }

        comment.gif?.let { gif ->
            CommentGifView(
                gif = gif,
                modifier = Modifier
                    .padding(top = 18.dp),
            )
        }

        CommentFooter(
            user = user,
            comment = comment,
            reactions = listReactions?.get(comment.id),
            userReaction = userReactions[comment.id],
            onReactionClick = { onReactionClick?.invoke(it, comment) },
            onReplyClick = { onReplyClick?.invoke(comment.user) },
            modifier = Modifier
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
                            user = user,
                            reply = reply,
                            reactions = listReactions?.get(reply.id),
                            userReaction = userReactions[reply.id],
                            progressEnabled = progressEnabled,
                            onRequestReactions = { onReplyLoaded?.invoke(reply) },
                            onReactionClick = { onReactionClick?.invoke(it, reply) },
                            onReplyClick = { onReplyClick?.invoke(reply.user) },
                            onDeleteClick = { onDeleteReplyClick?.invoke(reply) },
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
    userComment: Boolean,
    progressEnabled: Boolean,
    modifier: Modifier = Modifier,
    onEditClick: (() -> Unit)? = null,
    onDeleteClick: (() -> Unit)? = null,
    onReportClick: (() -> Unit)? = null,
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
                comment.user.isAnyVip -> TraktTheme.colors.vipAccent
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
        }

        Column(verticalArrangement = Arrangement.Absolute.spacedBy(1.dp)) {
            Row(
                horizontalArrangement = Arrangement.Absolute.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = comment.user.displayName,
                    style = TraktTheme.typography.paragraph.copy(fontWeight = FontWeight.W600),
                    color = TraktTheme.colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text = comment.createdAt.toLocal().format(longDateTimeFormat()).capitalize(),
                style = TraktTheme.typography.meta,
                color = TraktTheme.colors.textSecondary
                    .copy(alpha = 0.66f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Absolute.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Spacer(modifier = Modifier.weight(1f))
            CommentUserChips(
                comment = comment,
                progressVisible = progressEnabled && !userComment,
            )

            val menuEditClick = onEditClick.takeIf { userComment }
            val menuDeleteClick = onDeleteClick.takeIf { userComment }
            val menuReportClick = onReportClick.takeIf { !userComment }
            if (menuEditClick != null || menuDeleteClick != null || menuReportClick != null) {
                CommentDropdown(
                    deleteText = R.string.button_text_delete_note,
                    onEditClick = menuEditClick,
                    onDeleteClick = menuDeleteClick,
                    onReportClick = menuReportClick,
                )
            }
        }
    }
}

@Composable
private fun CommentFooter(
    user: User?,
    comment: Comment,
    reactions: ReactionsSummary?,
    userReaction: Reaction?,
    modifier: Modifier = Modifier,
    onReplyClick: (() -> Unit)? = null,
    onReactionClick: ((Reaction) -> Unit)? = null,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val tooltipState = rememberTooltipState(isPersistent = true)

    val reactionsEnabled =
        user != null && user.ids.trakt != comment.user.ids.trakt

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier.fillMaxWidth(),
    ) {
        ReactionsToolTip(
            state = tooltipState,
            reactions = reactions,
            userReaction = userReaction,
            onReactionClick = onReactionClick,
        ) {
            ReactionsSummaryChip(
                reactions = reactions,
                userReaction = userReaction,
                enabled = reactionsEnabled,
                modifier = Modifier.onClick {
                    if (reactions == null || !reactionsEnabled) {
                        return@onClick
                    }
                    scope.launch {
                        if (tooltipState.isVisible) {
                            tooltipState.dismiss()
                        } else {
                            tooltipState.show()
                        }
                    }
                },
            )
        }

        Row(
            horizontalArrangement = spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (comment.rememberTranslatable()) {
                Icon(
                    painter = painterResource(R.drawable.ic_translate),
                    contentDescription = "Replies",
                    tint = TraktTheme.colors.textPrimary,
                    modifier = Modifier
                        .size(18.dp)
                        .onClick {
                            val activityInfo = context.googleTranslateActivityInfo()
                            activityInfo?.let {
                                context.openGoogleTranslate(
                                    activity = activityInfo,
                                    text = comment.comment.trim(),
                                )
                            }
                        },
                )
            }

            if (reactionsEnabled) {
                Icon(
                    painter = painterResource(R.drawable.ic_comment_plus),
                    contentDescription = null,
                    tint = TraktTheme.colors.textPrimary,
                    modifier = Modifier
                        .size(18.dp)
                        .onClick {
                            onReplyClick?.invoke()
                        },
                )
            }
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
                        loading = LoadingState.Loading,
                    ),
                )

                CommentDetailsViewContent(
                    state = CommentDetailsState(
                        user = PreviewData.user1,
                        comment = PreviewData.comment1.copy(
                            gif = CommentGif(
                                url = "https://example.com/gif.gif",
                                size = 480 to 270,
                            ),
                        ),
                        replies = listOf(
                            PreviewData.comment1,
                        ).toImmutableList(),
                        loading = Done,
                    ),
                )

                CommentDetailsViewContent(
                    state = CommentDetailsState(
                        comment = PreviewData.comment1,
                        replies = listOf(
                            PreviewData.comment1,
                        ).toImmutableList(),
                        loading = Done,
                    ),
                    progressEnabled = true,
                )
            }
        }
    }
}
