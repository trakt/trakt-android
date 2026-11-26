package tv.trakt.trakt.core.profile.sections.leaderboard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.usertier.AvatarVariant
import tv.trakt.trakt.ui.components.usertier.UserAvatar
import tv.trakt.trakt.ui.theme.TraktTheme

private val PillShape = RoundedCornerShape(100)
private val PillHeight = 32.dp
private val PillHeightWithSublabel = 34.dp
private val AvatarSize = 25.dp
private val AvatarOverlap = 14.dp

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
    UserAvatar(
        user = user,
        variant = AvatarVariant.Thumb,
        borderWidth = 1.5.dp,
        borderColor = when {
            user.isAnyVip -> TraktTheme.colors.vipAccent
            else -> TraktTheme.colors.chipContainer
        },
        modifier = modifier.size(AvatarSize),
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
        }
    }
}
