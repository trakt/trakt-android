package tv.trakt.trakt.core.profile.sections.leaderboard.ui

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil3.compose.AsyncImage
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

private val PillShape = RoundedCornerShape(100)
private val PillHeight = 32.dp
private val PillHeightWithSublabel = 32.dp
private val AvatarSize = 23.dp
private val AvatarOverlap = 14.dp
private val SkeletonWidth = 145.dp

@Composable
internal fun LeaderboardPill(
    modifier: Modifier = Modifier,
    users: ImmutableList<User> = persistentListOf(),
    followingCount: Int? = null,
    onClick: () -> Unit = {},
) {
    val containerColor = TraktTheme.colors.chipContainer.copy(alpha = 0.4F)
    val contentColor = TraktTheme.colors.chipContent
    val height = if (followingCount != null) PillHeightWithSublabel else PillHeight

    Row(
        verticalAlignment = CenterVertically,
        horizontalArrangement = spacedBy(6.dp),
        modifier = modifier
            .height(height)
            .clip(PillShape)
            .background(containerColor, PillShape)
            .onClick(onClick = onClick)
            .padding(
                start = if (users.isEmpty()) 9.dp else (height - AvatarSize) / 2,
                end = 4.dp,
            ),
    ) {
        if (users.isEmpty()) {
            Icon(
                painter = painterResource(R.drawable.ic_leaderboard),
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(16.dp),
            )
        } else {
            StackedAvatars(
                users = users,
                modifier = Modifier.padding(end = 1.dp),
            )
        }

        Column(
            verticalArrangement = spacedBy(0.dp),
        ) {
            Text(
                text = stringResource(R.string.text_leaderboard),
                color = TraktTheme.colors.textPrimary,
                style = TraktTheme.typography.meta,
                maxLines = 1,
            )

            if (followingCount != null) {
                Text(
                    text = stringResource(R.string.text_leaderboard_following_count, followingCount),
                    color = TraktTheme.colors.textSecondary,
                    style = TraktTheme.typography.meta.copy(
                        fontSize = 9.sp,
                    ),
                    maxLines = 1,
                )
            }
        }

        Icon(
            painter = painterResource(R.drawable.ic_chevron_right),
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(12.dp),
        )
    }
}

@Composable
internal fun LeaderboardPillSkeleton(modifier: Modifier = Modifier) {
    val shimmerColor by rememberInfiniteTransition(label = "infiniteTransition")
        .animateColor(
            initialValue = TraktTheme.colors.skeletonContainer,
            targetValue = TraktTheme.colors.skeletonShimmer,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1000),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "shimmerTransition",
        )

    Box(
        modifier = modifier
            .size(width = SkeletonWidth, height = PillHeightWithSublabel)
            .background(shimmerColor, PillShape),
    )
}

@Composable
private fun StackedAvatars(
    users: ImmutableList<User>,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = CenterVertically,
        horizontalArrangement = spacedBy(-AvatarOverlap),
        modifier = modifier,
    ) {
        users.forEachIndexed { index, user ->
            StackedAvatar(
                user = user,
                modifier = Modifier.zIndex((users.size - index).toFloat()),
            )
        }
    }
}

@Composable
private fun StackedAvatar(
    user: User,
    modifier: Modifier = Modifier,
) {
    val borderColor = when {
        user.isAnyVip -> TraktTheme.colors.vipAccent
        else -> TraktTheme.colors.chipContainer
    }
    val avatarModifier = modifier
        .size(AvatarSize)
        .border(1.25.dp, borderColor, CircleShape)
        .clip(CircleShape)

    val avatar = user.images?.avatar?.full
    if (avatar == null) {
        Image(
            painter = painterResource(R.drawable.ic_person_placeholder),
            contentDescription = null,
            modifier = avatarModifier,
        )
        return
    }

    AsyncImage(
        model = remember(avatar) {
            when {
                avatar.startsWith("http") -> avatar
                else -> "https://$avatar"
            }
                .replace("/medium/", "/thumb/")
                .replace("/original/", "/thumb/")
        },
        contentDescription = null,
        contentScale = ContentScale.Crop,
        error = painterResource(R.drawable.ic_person_placeholder),
        modifier = avatarModifier,
    )
}

@PreviewTraktLightDark
@Composable
private fun Preview() {
    TraktThemePreview {
        Column(
            verticalArrangement = spacedBy(8.dp),
            modifier = Modifier.padding(16.dp),
        ) {
            LeaderboardPill()
            LeaderboardPill(
                users = persistentListOf(
                    PreviewData.user1,
                    PreviewData.user1.copy(images = null),
                    PreviewData.user1,
                ),
            )
            LeaderboardPill(
                users = persistentListOf(
                    PreviewData.user1.copy(isVip = true),
                    PreviewData.user1.copy(images = null),
                    PreviewData.user1,
                ),
                followingCount = 8,
            )
            LeaderboardPill(
                followingCount = 8,
            )
            LeaderboardPillSkeleton()
        }
    }
}
