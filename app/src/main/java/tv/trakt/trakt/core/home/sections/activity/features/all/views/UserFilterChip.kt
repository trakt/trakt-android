package tv.trakt.trakt.core.home.sections.activity.features.all.views

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.ui.components.usertier.AvatarVariant
import tv.trakt.trakt.ui.components.usertier.UserAvatar
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun UserFilterChip(
    user: User,
    selected: Boolean,
    modifier: Modifier = Modifier,
    height: Dp = 32.dp,
    onClick: () -> Unit = {},
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = spacedBy(0.dp),
        modifier = modifier
            .onClick { onClick() }
            .height(height)
            .border(
                width = 1.dp,
                color = if (selected) {
                    TraktTheme.colors.accent
                } else {
                    TraktTheme.colors.chipContainer
                },
                shape = CircleShape,
            )
            .background(
                shape = RoundedCornerShape(100),
                color = if (selected) {
                    TraktTheme.colors.accent
                } else {
                    Color.Transparent
                },
            )
            .padding(
                start = 6.dp,
                end = 12.dp,
            ),
    ) {
        val borderWidth = remember(selected) {
            if (selected) 1.dp else 0.dp
        }

        val borderColor = remember(selected) {
            if (selected) Color.White else Color.Transparent
        }

        UserAvatar(
            user = user,
            borderWidth = borderWidth,
            borderColor = borderColor,
            ringColor = if (selected) Color.White else null,
            variant = AvatarVariant.Thumb,
            modifier = Modifier.size(20.dp),
        )

        Text(
            text = user.displayName,
            style = TraktTheme.typography.buttonTertiary,
            color = TraktTheme.colors.textPrimary,
            maxLines = 1,
            overflow = Ellipsis,
            textAlign = TextAlign.Start,
            modifier = Modifier
                .widthIn(max = 164.dp)
                .padding(start = 6.dp),
        )
    }
}

@Preview
@Composable
private fun Preview() {
    TraktTheme {
        UserFilterChip(
            user = PreviewData.user1,
            selected = false,
        )
    }
}

@Preview
@Composable
private fun Preview2() {
    TraktTheme {
        UserFilterChip(
            user = PreviewData.user1,
            selected = true,
        )
    }
}
