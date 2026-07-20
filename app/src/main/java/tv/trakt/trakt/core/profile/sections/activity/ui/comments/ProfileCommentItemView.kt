package tv.trakt.trakt.core.profile.sections.activity.ui.comments

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults.cardColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.W700
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import coil3.ColorImage
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.launch
import tv.trakt.trakt.common.helpers.extensions.capitalize
import tv.trakt.trakt.common.helpers.extensions.longDateFormat
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.extensions.toLocal
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.Comment
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Images.Size
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.reactions.Reaction
import tv.trakt.trakt.common.model.reactions.ReactionsSummary
import tv.trakt.trakt.core.profile.sections.activity.model.ProfileCommentItem
import tv.trakt.trakt.core.profile.sections.activity.model.ProfileCommentItem.EpisodeItem
import tv.trakt.trakt.core.profile.sections.activity.model.ProfileCommentItem.MovieItem
import tv.trakt.trakt.core.profile.sections.activity.model.ProfileCommentItem.ShowItem
import tv.trakt.trakt.core.reactions.ui.ReactionsSummaryChip
import tv.trakt.trakt.core.reactions.ui.ReactionsToolTip
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.mediacards.HorizontalMediaCard
import tv.trakt.trakt.ui.components.mediacards.VerticalMediaCard
import tv.trakt.trakt.ui.theme.DefaultCardShape
import tv.trakt.trakt.ui.theme.TraktTheme

private val EmptyReactionsSummary = emptyMap<Int, ReactionsSummary>().toImmutableMap()
private val EmptyReactions = emptyMap<Int, Reaction?>().toImmutableMap()

@Composable
internal fun ProfileCommentItemView(
    item: ProfileCommentItem,
    modifier: Modifier = Modifier,
    reactions: ImmutableMap<Int, ReactionsSummary> = EmptyReactionsSummary,
    onClick: () -> Unit = {},
    onShowClick: ((Show) -> Unit)? = null,
    onMovieClick: ((Movie) -> Unit)? = null,
    onEpisodeClick: ((Show, Episode) -> Unit)? = null,
    onRequestReactions: ((Comment) -> Unit)? = null,
    onRepliesClick: (() -> Unit)? = null,
    onDeleteClick: (() -> Unit)? = null,
) {
    LaunchedEffect(item.comment.id) {
        if (reactions[item.comment.id] == null) {
            onRequestReactions?.invoke(item.comment)
        }
    }

    Card(
        onClick = onClick,
        modifier = modifier,
        shape = DefaultCardShape,
        colors = cardColors(
            containerColor = TraktTheme.colors.commentContainer,
        ),
        content = {
            CommentCardContent(
                item = item,
                reactions = reactions,
                onShowClick = onShowClick,
                onMovieClick = onMovieClick,
                onEpisodeClick = onEpisodeClick,
                onRepliesClick = onRepliesClick,
                onDeleteClick = onDeleteClick,
            )
        },
    )
}

@Composable
private fun CommentCardContent(
    item: ProfileCommentItem,
    reactions: ImmutableMap<Int, ReactionsSummary>,
    modifier: Modifier = Modifier,
    onShowClick: ((Show) -> Unit)? = null,
    onMovieClick: ((Movie) -> Unit)? = null,
    onEpisodeClick: ((Show, Episode) -> Unit)? = null,
    onRepliesClick: (() -> Unit)? = null,
    onDeleteClick: (() -> Unit)? = null,
) {
    Column(
        verticalArrangement = spacedBy(0.dp),
        modifier = modifier
            .padding(vertical = 16.dp)
            .fillMaxSize(),
    ) {
        CommentHeader(
            item = item,
            onShowClick = onShowClick,
            onMovieClick = onMovieClick,
            onEpisodeClick = onEpisodeClick,
            onDeleteClick = onDeleteClick,
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        Text(
            text = item.comment.commentNoSpoilers,
            style = TraktTheme.typography.paragraphSmall.copy(
                fontSize = 13.sp,
                lineHeight = 1.3.em,
            ),
            color = TraktTheme.colors.textSecondary,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(top = 11.dp, bottom = 20.dp),
        )

        Spacer(modifier = Modifier.weight(1f))

        CommentFooter(
            comment = item.comment,
            reactions = reactions[item.comment.id],
            onRepliesClick = onRepliesClick,
            modifier = Modifier
                .padding(
                    start = 16.dp,
                    end = 20.dp,
                ),
        )
    }
}

@Composable
private fun CommentHeader(
    item: ProfileCommentItem,
    modifier: Modifier = Modifier,
    onShowClick: ((Show) -> Unit)? = null,
    onMovieClick: ((Movie) -> Unit)? = null,
    onEpisodeClick: ((Show, Episode) -> Unit)? = null,
    onDeleteClick: (() -> Unit)? = null,
) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = spacedBy(10.dp),
        modifier = modifier,
    ) {
        if (item is EpisodeItem) {
            HorizontalMediaCard(
                title = "",
                width = 112.dp,
                corner = 8.dp,
                containerImageUrl = item.episode.images?.getScreenshotUrl(Size.THUMB)
                    ?: item.show.images?.getFanartUrl(),
                more = false,
                onClick = {
                    onEpisodeClick?.invoke(item.show, item.episode)
                },
                modifier = Modifier
                    .shadow(2.dp, RoundedCornerShape(8.dp)),
            )
        } else {
            VerticalMediaCard(
                title = "",
                width = 42.dp,
                corner = 8.dp,
                imageUrl = item.images?.getPosterUrl(Size.THUMB),
                more = false,
                onClick = {
                    when (item) {
                        is MovieItem -> onMovieClick?.invoke(item.movie)
                        is ShowItem -> onShowClick?.invoke(item.show)
                    }
                },
                modifier = Modifier
                    .shadow(2.dp, RoundedCornerShape(8.dp)),
            )
        }

        Column(
            verticalArrangement = spacedBy(1.dp),
            modifier = Modifier
                .weight(1f)
                .padding(end = 16.dp)
                .onClick {
                    when (item) {
                        is ShowItem -> onShowClick?.invoke(item.show)
                        is MovieItem -> onMovieClick?.invoke(item.movie)
                        is EpisodeItem -> onEpisodeClick?.invoke(item.show, item.episode)
                    }
                },
        ) {
            Row(
                horizontalArrangement = spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = item.title,
                    style = TraktTheme.typography.paragraph.copy(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.W500,
                    ),
                    color = TraktTheme.colors.textPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            val dateFormat = longDateFormat()
            Text(
                text = remember(item.comment.createdAt) {
                    item.comment.createdAt.toLocal().format(dateFormat).capitalize()
                },
                style = TraktTheme.typography.meta,
                color = TraktTheme.colors.textSecondary
                    .copy(alpha = 0.66f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

//        Row(
//            verticalAlignment = Alignment.CenterVertically,
//            horizontalArrangement = spacedBy(12.dp),
//        ) {
//            Icon(
//                painter = painterResource(R.drawable.ic_trash),
//                contentDescription = null,
//                tint = TraktTheme.colors.textPrimary,
//                modifier = Modifier
//                    .size(20.dp)
//                    .onClick {
//                        onDeleteClick?.invoke()
//                    },
//            )
//        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CommentFooter(
    comment: Comment,
    reactions: ReactionsSummary?,
    modifier: Modifier = Modifier,
    onReactionClick: ((Reaction, Comment) -> Unit)? = null,
    onRepliesClick: (() -> Unit)? = null,
) {
    val scope = rememberCoroutineScope()
    val tooltipState = rememberTooltipState(isPersistent = true)

    Column(
        modifier = modifier,
        verticalArrangement = spacedBy(22.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 1.dp),
        ) {
            ReactionsToolTip(
                state = tooltipState,
                userEnabled = false,
                reactions = reactions,
                onReactionClick = { reaction ->
                    onReactionClick?.invoke(reaction, comment)
                },
            ) {
                ReactionsSummaryChip(
                    enabled = false,
                    reactions = reactions,
                    modifier = Modifier.onClick {
                        if (reactions == null) {
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
                horizontalArrangement = spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (comment.replies > 0) {
                    Row(
                        horizontalArrangement = spacedBy(2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.onClick {
                            onRepliesClick?.invoke()
                        },
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
        }
    }
}

@OptIn(ExperimentalCoilApi::class)
@Preview
@Composable
fun CommentPreview() {
    TraktTheme {
        val previewHandler = AsyncImagePreviewHandler {
            ColorImage(Color.LightGray.toArgb())
        }
        CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
            Column(
                verticalArrangement = spacedBy(16.dp),
            ) {
                ProfileCommentItemView(
                    item = ShowItem(
                        show = PreviewData.show1,
                        comment = PreviewData.comment1.copy(userRating = 1, comment = "Lorem Ipsum"),
                    ),
                    modifier = Modifier
                        .height(TraktTheme.size.commentCardSize),
                )

                ProfileCommentItemView(
                    item = ProfileCommentItem.EpisodeItem(
                        show = PreviewData.show1,
                        episode = PreviewData.episode1,
                        comment = PreviewData.comment1.copy(userRating = 1, comment = "Lorem Ipsum"),
                    ),
                    modifier = Modifier
                        .height(TraktTheme.size.commentCardSize),
                )
            }
        }
    }
}
