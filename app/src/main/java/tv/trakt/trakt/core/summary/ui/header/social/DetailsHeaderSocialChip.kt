package tv.trakt.trakt.core.summary.ui.header.social

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.BottomEnd
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil3.compose.AsyncImage
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.ui.theme.colors.Purple600
import tv.trakt.trakt.core.summary.social.model.MediaSocialActivity
import tv.trakt.trakt.core.summary.social.recommendedby.model.RecommendedBy
import tv.trakt.trakt.helpers.extensions.TraktThemeLightDark
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

private const val USERS_LIMIT = 3

private val ShareBadgeSize = 10.dp
private val ShareBadgeIconSize = 7.dp
private val ShareBadgeOffset = 0.dp

/**
 * How the chip reflects people who shared the item with the user.
 */
internal enum class SocialChipShare {
    None,

    /** Shared with the user, alongside followed activity. */
    Shared,

    /** Shared with the user, without any followed activity. */
    SharedOnly,
    ;

    companion object {
        fun of(
            recommendedBy: RecommendedBy?,
            socials: List<MediaSocialActivity>?,
        ): SocialChipShare {
            return when {
                recommendedBy?.hasSharers != true -> None
                socials.isNullOrEmpty() -> SharedOnly
                else -> Shared
            }
        }
    }
}

/**
 * Avatars for the chip: the first sharer when only shared, otherwise the followed users.
 */
internal fun socialChipUsers(
    share: SocialChipShare,
    socials: List<MediaSocialActivity>?,
    recommendedBy: RecommendedBy?,
): ImmutableList<User>? {
    return when (share) {
        SocialChipShare.SharedOnly -> recommendedBy?.users?.take(1)?.toImmutableList()
        SocialChipShare.None, SocialChipShare.Shared -> socials?.map { it.user }?.toImmutableList()
    }
}

@Composable
internal fun DetailsSocialChip(
    modifier: Modifier = Modifier,
    users: ImmutableList<User>,
    share: SocialChipShare = SocialChipShare.None,
    size: Dp = 28.dp,
    spacing: Int = 16,
) {
    val limitUsers = remember(users.size) { users.take(USERS_LIMIT) }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = spacedBy(6.dp),
    ) {
        Box {
            limitUsers.forEachIndexed { index, user ->
                val offset = (index * spacing).dp
                AsyncImage(
                    model = user.images?.avatar?.full,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(R.drawable.ic_person_placeholder),
                    error = painterResource(R.drawable.ic_person_placeholder),
                    modifier = Modifier
                        .zIndex(10 - index.toFloat())
                        .padding(start = offset)
                        .size(size)
                        .border(
                            width = 1.25.dp,
                            color = if (user.isAnyVip) TraktTheme.colors.vipAccent else Color.Transparent,
                            shape = CircleShape,
                        )
                        .clip(CircleShape),
                )
            }

            if (share != SocialChipShare.None && limitUsers.isNotEmpty()) {
                ShareBadge(
                    modifier = Modifier
                        .align(BottomEnd)
                        .offset(x = ShareBadgeOffset, y = ShareBadgeOffset)
                        .zIndex(20F),
                )
            }
        }

        when {
            share == SocialChipShare.SharedOnly -> {
                Text(
                    text = stringResource(R.string.text_shared_with_you),
                    color = TraktTheme.colors.textPrimary,
                    style = TraktTheme.typography.meta.copy(fontSize = 12.sp),
                )
            }

            users.isNotEmpty() -> {
                UsersCount(
                    count = users.size,
                )
            }
        }
    }
}

@Composable
private fun ShareBadge(modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(ShareBadgeSize)
            .background(Purple600, CircleShape),
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_share),
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(ShareBadgeIconSize),
        )
    }
}

@Composable
private fun UsersCount(count: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = spacedBy(0.25.dp),
    ) {
        Text(
            text = when {
                count == 1 -> "$count ${stringResource(R.string.text_social_activity)}"
                else -> "$count ${stringResource(R.string.text_social_activities)}"
            },
            color = TraktTheme.colors.textPrimary,
            style = TraktTheme.typography.meta.copy(fontSize = 12.sp),
        )
    }
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF212427,
)
@Composable
private fun DetailsSocialChipPreview() {
    TraktThemeLightDark {
        Column(
            verticalArrangement = spacedBy(16.dp),
        ) {
            DetailsSocialChip(
                users = listOf(
                    PreviewData.user1,
                    PreviewData.user1.copy(isVip = true),
                    PreviewData.user1.copy(isVip = true),
                    PreviewData.user1.copy(isVip = true),
                    PreviewData.user1.copy(isVip = true),
                    PreviewData.user1.copy(isVip = true),
                )
                    .toImmutableList(),
            )

            DetailsSocialChip(
                users = listOf(
                    PreviewData.user1,
                    PreviewData.user1.copy(isVip = true),
                )
                    .toImmutableList(),
                share = SocialChipShare.Shared,
            )

            DetailsSocialChip(
                users = listOf(PreviewData.user1).toImmutableList(),
                share = SocialChipShare.SharedOnly,
            )
        }
    }
}
