package tv.trakt.trakt.core.today.stories.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.extensions.relativePastDateString
import tv.trakt.trakt.common.helpers.extensions.toLocal
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.core.today.model.FollowingActivity.Detail
import tv.trakt.trakt.core.today.model.FollowingActivity.Target
import tv.trakt.trakt.core.today.model.TodayFriendAction
import tv.trakt.trakt.core.today.model.TodayMilestone
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

private const val MAX_FACES = 5

private val AvatarSize = 52.dp
private val FaceSize = 36.dp
private val FaceOverlap = 12.dp
private val AvatarBorder = 2.dp
private val InlineStarSize = 14.dp

@Composable
internal fun TodayFriendFooter(
    action: TodayFriendAction,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = spacedBy(12.dp),
        modifier = modifier,
    ) {
        TodayAvatar(
            user = action.user,
            size = AvatarSize,
            borderColor = TraktTheme.colors.accent,
        )

        Column(
            verticalArrangement = spacedBy(1.dp),
            modifier = Modifier.weight(1F),
        ) {
            Text(
                text = action.user.displayName,
                style = TraktTheme.typography.cardTitle.copy(
                    fontSize = 14.sp,
                ),
                color = TraktTheme.colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = spacedBy(3.dp),
            ) {
                Text(
                    text = friendActionLabel(action),
                    style = TraktTheme.typography.cardTitle.copy(
                        fontSize = 14.sp,
                    ),
                    color = TraktTheme.colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1F, fill = false),
                )

                action.rating?.let {
                    if (action.episode != null || action.season != null) {
                        Text(
                            text = "•",
                            style = TraktTheme.typography.cardTitle,
                            color = TraktTheme.colors.textSecondary,
                        )
                    }
                    TodayInlineRating(rating10 = it.toFloat())
                }
            }
            Text(
                text = friendActionTime(action.activityAt),
                style = TraktTheme.typography.cardSubtitle,
                color = TraktTheme.colors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        action.milestone?.let {
            TodayMilestoneChip(milestone = it)
        }
    }
}

@Composable
internal fun TodayFaces(
    users: ImmutableList<User>,
    modifier: Modifier = Modifier,
) {
    val background = TraktTheme.colors.backgroundPrimary

    Box(modifier = modifier) {
        users.take(MAX_FACES).forEachIndexed { index, user ->
            TodayAvatar(
                user = user,
                size = FaceSize,
                borderColor = background,
                modifier = Modifier.offset(x = (FaceSize - FaceOverlap) * index),
            )
        }

        Box(
            modifier = Modifier.size(
                width = FaceSize + (FaceSize - FaceOverlap) * (minOf(users.size, MAX_FACES) - 1),
                height = FaceSize,
            ),
        )
    }
}

@Composable
private fun TodayAvatar(
    user: User,
    size: Dp,
    borderColor: Color,
    modifier: Modifier = Modifier,
) {
    val imageModifier = modifier
        .size(size)
        .border(AvatarBorder, borderColor, CircleShape)
        .padding(AvatarBorder)
        .clip(CircleShape)

    if (user.hasAvatar) {
        AsyncImage(
            model = user.images?.avatar?.full,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            error = painterResource(R.drawable.ic_person_placeholder),
            modifier = imageModifier,
        )
    } else {
        Image(
            painter = painterResource(R.drawable.ic_person_placeholder),
            contentDescription = null,
            modifier = imageModifier,
        )
    }
}

@Composable
internal fun TodayRatingTag(
    rating10: Float,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = spacedBy(2.dp),
        modifier = modifier
            .background(TraktTheme.colors.tagChipContainer, RoundedCornerShape(50))
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_star_trakt_on),
            contentDescription = null,
            tint = TraktTheme.colors.tagChipContent,
            modifier = Modifier.size(14.dp),
        )
        Text(
            text = starsText(rating10),
            style = TraktTheme.typography.meta,
            color = TraktTheme.colors.tagChipContent,
        )
    }
}

@Composable
private fun TodayInlineRating(rating10: Float) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = spacedBy(1.5.dp),
    ) {
        Text(
            text = starsText(rating10),
            style = TraktTheme.typography.cardTitle.copy(
                fontSize = 14.sp,
            ),
            color = TraktTheme.colors.textPrimary,
            maxLines = 1,
        )

        Icon(
            painter = painterResource(R.drawable.ic_star_trakt_on),
            contentDescription = null,
            tint = TraktTheme.colors.textPrimary,
            modifier = Modifier.size(InlineStarSize),
        )
    }
}

private fun starsText(rating10: Float): String {
    return "%.1f"
        .format(Locale.US, rating10 / 2F)
        .removeSuffix(".0")
}

@Composable
private fun friendActionLabel(action: TodayFriendAction): String {
    val item = friendItemLabel(action)

    return when (val detail = action.detail) {
        Detail.Watch -> when {
            action.episodeCount > 1 -> stringResource(
                R.string.text_today_watched_episodes,
                action.episodeCount,
            )
            item != null -> stringResource(R.string.text_today_watched_episode, item)
            else -> stringResource(R.string.text_today_watched)
        }

        is Detail.Rating -> when (item) {
            null -> stringResource(R.string.text_today_rated)
            else -> stringResource(R.string.text_today_rated_item, item)
        }

        is Detail.Comment -> when {
            detail.isReview -> stringResource(R.string.text_today_reviewed)
            item != null -> stringResource(R.string.text_today_commented_item, item)
            else -> stringResource(R.string.text_today_commented)
        }
    }
}

@Composable
private fun friendItemLabel(action: TodayFriendAction): String? {
    action.episode?.let {
        return stringResource(R.string.episode_footer_season_episode, it.season, it.number)
    }
    action.season?.let {
        return stringResource(R.string.text_season_number, it.number)
    }
    return null
}

/** Clock time for today, prefixed with the relative day otherwise. */
@Composable
private fun friendActionTime(activityAt: Instant): String {
    val local = activityAt.toLocal()
    val time = local.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))

    if (local.toLocalDate() == LocalDate.now()) return time

    return stringResource(
        R.string.text_today_day_time,
        local.relativePastDateString(),
        time,
    )
}

private val previewUser = PreviewData.user1.copy(
    images = User.Images(
        avatar = User.Image(full = "https://example.com/avatar.jpg"),
    ),
)

private fun previewAction(
    target: Target = Target.ShowTarget(PreviewData.show1),
    detail: Detail = Detail.Watch,
    episodeCount: Int = 0,
    milestone: TodayMilestone? = null,
    activityAt: Instant = Instant.now(),
): TodayFriendAction {
    return TodayFriendAction(
        key = "preview",
        user = previewUser,
        activityAt = activityAt,
        target = target,
        detail = detail,
        episodeCount = episodeCount,
        milestone = milestone,
    )
}

private val previewComment = Detail.Comment(
    id = 1,
    text = "Loved it",
    gifUrl = null,
    isSpoiler = false,
    isReview = false,
    likeCount = 0,
    replyCount = 0,
)

@Composable
private fun PreviewColumn(content: @Composable () -> Unit) {
    TraktTheme {
        Column(
            verticalArrangement = spacedBy(16.dp),
            modifier = Modifier.padding(16.dp),
        ) {
            content()
        }
    }
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFF131517,
    widthDp = 360,
)
@Composable
private fun WatchPreview() {
    PreviewColumn {
        TodayFriendFooter(
            action = previewAction(
                target = Target.MovieTarget(PreviewData.movie1),
            ),
        )
        TodayFriendFooter(
            action = previewAction(
                target = Target.EpisodeTarget(PreviewData.show1, PreviewData.episode1),
                episodeCount = 1,
            ),
        )
        TodayFriendFooter(
            action = previewAction(
                target = Target.EpisodeTarget(PreviewData.show1, PreviewData.episode1),
                episodeCount = 4,
            ),
        )
    }
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFF131517,
    widthDp = 360,
)
@Composable
private fun MilestonePreview() {
    PreviewColumn {
        TodayFriendFooter(
            action = previewAction(
                target = Target.EpisodeTarget(PreviewData.show1, PreviewData.episode1),
                episodeCount = 1,
                milestone = TodayMilestone(TodayMilestone.Type.SeriesStart, season = 1),
            ),
        )
        TodayFriendFooter(
            action = previewAction(
                target = Target.EpisodeTarget(PreviewData.show1, PreviewData.episode1),
                episodeCount = 8,
                milestone = TodayMilestone(TodayMilestone.Type.SeasonEnd, season = 2),
            ),
        )
    }
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFF131517,
    widthDp = 360,
)
@Composable
private fun RatingAndCommentPreview() {
    PreviewColumn {
        TodayFriendFooter(
            action = previewAction(detail = Detail.Rating(rating = 8)),
        )
        TodayFriendFooter(
            action = previewAction(
                target = Target.SeasonTarget(PreviewData.show1, PreviewData.season1),
                detail = Detail.Rating(rating = 6),
            ),
        )
        TodayFriendFooter(
            action = previewAction(
                target = Target.EpisodeTarget(PreviewData.show1, PreviewData.episode1),
                detail = previewComment,
            ),
        )
        TodayFriendFooter(
            action = previewAction(detail = previewComment.copy(isReview = true)),
        )
    }
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFF131517,
    widthDp = 360,
)
@Composable
private fun TimePreview() {
    PreviewColumn {
        TodayFriendFooter(
            action = previewAction(
                activityAt = Instant.now().minus(1, ChronoUnit.DAYS),
            ),
        )
        TodayFriendFooter(
            action = previewAction().copy(
                user = PreviewData.user1.copy(
                    name = "A friend with a very long display name that does not fit",
                ),
            ),
        )
    }
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun FacesPreview() {
    PreviewColumn {
        TodayFaces(users = persistentListOf(previewUser))
        TodayFaces(users = persistentListOf(previewUser, PreviewData.user1, previewUser))
        TodayFaces(users = List(8) { previewUser }.toImmutableList())
    }
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun RatingTagPreview() {
    PreviewColumn {
        TodayRatingTag(rating10 = 10F)
        TodayRatingTag(rating10 = 7F)
        TodayRatingTag(rating10 = 6.4F)
    }
}
