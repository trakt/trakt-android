@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.comments.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardDefaults.cardColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import coil3.ColorImage
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import kotlinx.coroutines.launch
import tv.trakt.trakt.common.helpers.extensions.DevicePreview
import tv.trakt.trakt.common.helpers.extensions.DevicePreviewRtl
import tv.trakt.trakt.common.helpers.extensions.capitalize
import tv.trakt.trakt.common.helpers.extensions.googleTranslateActivityInfo
import tv.trakt.trakt.common.helpers.extensions.highlightMentions
import tv.trakt.trakt.common.helpers.extensions.longDateTimeFormat
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.extensions.openGoogleTranslate
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.Comment
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.reactions.Reaction
import tv.trakt.trakt.common.model.reactions.ReactionsSummary
import tv.trakt.trakt.core.comments.features.report.ReportCommentSheet
import tv.trakt.trakt.core.reactions.ui.ReactionsSummaryChip
import tv.trakt.trakt.core.reactions.ui.ReactionsToolTip
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.DefaultCardShape
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun CommentReplyCard(
    user: User?,
    reply: Comment,
    modifier: Modifier = Modifier,
    reactions: ReactionsSummary? = null,
    userReaction: Reaction? = null,
    onClick: (() -> Unit)? = null,
    onUserClick: ((User) -> Unit)? = null,
    onRequestReactions: (() -> Unit)? = null,
    onReactionClick: ((Reaction) -> Unit)? = null,
    onReplyClick: (() -> Unit)? = null,
    onDeleteClick: (() -> Unit)? = null,
) {
    LaunchedEffect(reply.id) {
        if (reactions == null) {
            onRequestReactions?.invoke()
        }
    }

    var reportActive by remember { mutableStateOf(false) }

    val isUserReply = remember(reply.user) {
        user?.ids?.trakt == reply.user.ids.trakt
    }

    val colors = cardColors(containerColor = TraktTheme.colors.commentReplyContainer)
    val border = when {
        isUserReply -> BorderStroke(2.dp, TraktTheme.colors.accent)
        else -> null
    }
    val content: @Composable ColumnScope.() -> Unit = {
        CommentReplyCardContent(
            user = user,
            comment = reply,
            reactions = reactions,
            userReaction = userReaction,
            onReactionClick = onReactionClick,
            onReplyClick = onReplyClick,
            onDeleteClick = onDeleteClick,
            onReportClick = { reportActive = true },
            onUserClick = onUserClick,
        )
    }

    if (onClick == null) {
        Card(
            modifier = modifier,
            shape = DefaultCardShape,
            colors = colors,
            border = border,
            elevation = CardDefaults.cardElevation(
                defaultElevation = TraktTheme.colors.shadowDynamicSmall,
                pressedElevation = TraktTheme.colors.shadowDynamicSmall,
                disabledElevation = TraktTheme.colors.shadowDynamicSmall,
            ),
            content = content,
        )
    } else {
        Card(
            modifier = modifier,
            onClick = onClick,
            shape = DefaultCardShape,
            colors = colors,
            border = border,
            elevation = CardDefaults.cardElevation(
                defaultElevation = TraktTheme.colors.shadowDynamicSmall,
                pressedElevation = TraktTheme.colors.shadowDynamicSmall,
                disabledElevation = TraktTheme.colors.shadowDynamicSmall,
            ),
            content = content,
        )
    }

    ReportCommentSheet(
        active = reportActive,
        comment = reply,
        onDismiss = { reportActive = false },
    )
}

@Composable
private fun CommentReplyCardContent(
    user: User?,
    comment: Comment,
    reactions: ReactionsSummary?,
    userReaction: Reaction?,
    modifier: Modifier = Modifier,
    onUserClick: ((User) -> Unit)? = null,
    onReactionClick: ((Reaction) -> Unit)? = null,
    onReplyClick: (() -> Unit)? = null,
    onDeleteClick: (() -> Unit)? = null,
    onReportClick: (() -> Unit)? = null,
) {
    var showSpoilers by remember { mutableStateOf(false) }

    val isUserReply = remember(user) {
        comment.user.ids.trakt == user?.ids?.trakt
    }

    Column(
        verticalArrangement = spacedBy(0.dp),
        modifier = modifier.padding(vertical = 16.dp),
    ) {
        CommentHeader(
            user = user,
            comment = comment,
            onUserClick = onUserClick,
            onDeleteClick = onDeleteClick,
            onReportClick = onReportClick,
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        val mentionsColor = TraktTheme.colors.textPrimary
        val mentionsText = remember(comment) {
            comment.commentNoSpoilers.highlightMentions(mentionsColor)
        }

        CommentReplyBody(
            text = mentionsText,
            blurred = comment.hasSpoilers && !isUserReply && !showSpoilers,
            onRevealSpoiler = { showSpoilers = true },
        )

        CommentFooter(
            user = user,
            comment = comment,
            reactions = reactions,
            userReaction = userReaction,
            onReactionClick = onReactionClick,
            onReplyClick = onReplyClick,
            modifier = Modifier
                .padding(
                    top = 16.dp,
                    start = 16.dp,
                    end = 20.dp,
                ),
        )
    }
}

@Composable
private fun CommentReplyBody(
    text: AnnotatedString,
    blurred: Boolean,
    onRevealSpoiler: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val body = @Composable {
        Text(
            text = text,
            style = TraktTheme.typography.paragraphSmall.copy(lineHeight = 1.3.em),
            color = TraktTheme.colors.textSecondary,
            maxLines = Int.MAX_VALUE,
            modifier = modifier.then(
                if (blurred) {
                    Modifier
                        .blur(4.dp)
                        .padding(top = 12.dp)
                        .padding(horizontal = 16.dp)
                        .onClick { onRevealSpoiler() }
                } else {
                    Modifier
                        .padding(top = 12.dp)
                        .padding(horizontal = 16.dp)
                },
            ),
        )
    }

    if (blurred) {
        body()
    } else {
        SelectionContainer { body() }
    }
}

@Composable
private fun CommentHeader(
    user: User?,
    comment: Comment,
    modifier: Modifier = Modifier,
    onUserClick: ((User) -> Unit)? = null,
    onDeleteClick: (() -> Unit)? = null,
    onReportClick: (() -> Unit)? = null,
) {
    val isUserReply = remember {
        user?.ids?.trakt == comment.user.ids.trakt
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f, fill = false),
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .onClick {
                        onUserClick?.invoke(comment.user)
                    },
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

            Spacer(modifier = Modifier.size(10.dp))

            Column(verticalArrangement = spacedBy(2.dp)) {
                Row(
                    horizontalArrangement = spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = comment.user.displayName,
                        style = TraktTheme.typography.paragraph.copy(fontWeight = FontWeight.W600),
                        color = TraktTheme.colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.onClick {
                            onUserClick?.invoke(comment.user)
                        },
                    )
                }
                Text(
                    text = comment.createdAt.format(longDateTimeFormat()).capitalize(),
                    style = TraktTheme.typography.meta,
                    color = TraktTheme.colors.textSecondary
                        .copy(alpha = 0.66f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (isUserReply) {
                Spacer(modifier = Modifier.size(16.dp))

                Icon(
                    painter = painterResource(R.drawable.ic_trash),
                    contentDescription = null,
                    tint = TraktTheme.colors.textPrimary,
                    modifier = Modifier
                        .size(20.dp)
                        .onClick {
                            onDeleteClick?.invoke()
                        },
                )
            }

            Spacer(modifier = Modifier.size(16.dp))

            CommentDropdown(
                onReportClick = onReportClick,
            )
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
    onReactionClick: ((Reaction) -> Unit)? = null,
    onReplyClick: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val tooltipState = rememberTooltipState(isPersistent = true)

    val reactionsEnabled = remember(comment.user) {
        user != null && user.ids.trakt != comment.user.ids.trakt
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 1.dp),
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
                modifier = Modifier
                    .onClick {
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
            horizontalArrangement = Arrangement.spacedBy(20.dp),
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
@DevicePreview
@DevicePreviewRtl
@Composable
private fun Preview() {
    TraktTheme {
        val previewHandler = AsyncImagePreviewHandler {
            ColorImage(Color.LightGray.toArgb())
        }
        CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
            Column(
                verticalArrangement = spacedBy(16.dp),
            ) {
                CommentReplyCard(
                    onClick = {},
                    user = null,
                    reply = PreviewData.comment1
                        .copy(userRating = 1, comment = "Lorem Ipsum with @john"),
                )
                CommentReplyCard(
                    onClick = {},
                    user = PreviewData.user1,
                    reply = PreviewData.comment1.copy(userRating = 10),
                )

                CommentReplyCard(
                    onClick = {},
                    user = null,
                    reply = PreviewData.comment1.copy(userRating = 7),
                )
            }
        }
    }
}
