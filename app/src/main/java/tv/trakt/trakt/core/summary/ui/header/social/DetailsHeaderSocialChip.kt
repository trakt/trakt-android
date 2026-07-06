package tv.trakt.trakt.core.summary.ui.header.social

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
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
import tv.trakt.trakt.common.ui.theme.colors.Red500
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

private const val USERS_LIMIT = 5

@Composable
internal fun DetailsHeaderSocialHorizontalChip(
    modifier: Modifier = Modifier,
    users: ImmutableList<User>,
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
                            color = if (user.isAnyVip) Red500 else Color.Transparent,
                            shape = CircleShape,
                        )
                        .clip(CircleShape),
                )
            }
        }

        if (users.isNotEmpty()) {
            UsersCount(
                count = users.size,
            )
        }
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
private fun DetailsHeaderSocialHorizontalChipPreview() {
    TraktTheme {
        DetailsHeaderSocialHorizontalChip(
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
    }
}
