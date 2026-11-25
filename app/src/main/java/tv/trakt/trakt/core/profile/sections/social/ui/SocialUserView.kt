package tv.trakt.trakt.core.profile.sections.social.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.usertier.AvatarVariant
import tv.trakt.trakt.ui.components.usertier.UserAvatar
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun SocialUserView(
    user: User,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    showName: Boolean = true,
    onUserClick: () -> Unit = {},
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = spacedBy(6.dp),
        modifier = modifier
            .onClick { onUserClick() },
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(size),
        ) {
            UserAvatar(
                user = user,
                variant = AvatarVariant.Thumb,
                modifier = Modifier.size(size),
            )
        }

        if (showName) {
            Text(
                text = user.displayName,
                style = TraktTheme.typography.cardTitle,
                color = TraktTheme.colors.textPrimary,
                maxLines = 1,
                textAlign = TextAlign.Center,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.width(size + 8.dp),
            )
        }
    }
}

@Preview
@Composable
private fun Preview1() {
    TraktTheme {
        SocialUserView(
            user = PreviewData.user1,
        )
    }
}

@Preview
@Composable
private fun Preview2() {
    TraktTheme {
        SocialUserView(
            user = PreviewData.user1.copy(
                name = "John Dutton",
                isVip = true,
            ),
            showName = false,
        )
    }
}
