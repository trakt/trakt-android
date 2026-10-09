package tv.trakt.trakt.core.summary.social.recommendedby.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.core.profile.sections.social.ui.SocialUserView
import tv.trakt.trakt.helpers.extensions.TraktThemeLightDark
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

/**
 * Person who shared the item with the user. Styled after [MediaSocialItemCard], but not clickable:
 * the only action is hiding their recommendations.
 */
@Composable
internal fun SharedByItemCard(
    user: User,
    muting: Boolean,
    modifier: Modifier = Modifier,
    corner: Dp = 24.dp,
    containerColor: Color = TraktTheme.colors.panelCardContainer,
    onMuteClick: () -> Unit = {},
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .shadow(
                elevation = TraktTheme.colors.shadowSmall,
                shape = RoundedCornerShape(corner),
            )
            .graphicsLayer {
                clip = false
            }
            .background(containerColor, RoundedCornerShape(corner))
            .padding(12.dp),
    ) {
        SocialUserView(
            user = user,
            size = 52.dp,
            showName = false,
        )

        Text(
            text = user.displayName,
            style = TraktTheme.typography.cardTitle.copy(fontSize = 16.sp),
            color = TraktTheme.colors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1F)
                .padding(horizontal = 12.dp),
        )

        MuteMenu(
            muting = muting,
            onMuteClick = onMuteClick,
        )
    }
}

@Composable
private fun MuteMenu(
    muting: Boolean,
    onMuteClick: () -> Unit,
) {
    var showMenu by remember { mutableStateOf(false) }

    Box {
        Icon(
            painter = painterResource(R.drawable.ic_more_vertical),
            contentDescription = null,
            tint = TraktTheme.colors.textPrimary,
            modifier = Modifier
                .size(20.dp)
                .onClick { showMenu = true },
        )

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
            containerColor = TraktTheme.colors.dialogContainer,
            shape = RoundedCornerShape(16.dp),
        ) {
            DropdownMenuItem(
                enabled = !muting,
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_block),
                        contentDescription = null,
                        tint = TraktTheme.colors.textPrimary,
                        modifier = Modifier.size(22.dp),
                    )
                },
                text = {
                    Text(
                        text = stringResource(R.string.button_text_hide_their_recommendations),
                        style = TraktTheme.typography.buttonTertiary,
                        color = TraktTheme.colors.textPrimary,
                    )
                },
                onClick = {
                    showMenu = false
                    onMuteClick()
                },
            )
        }
    }
}

@Preview(widthDp = 360)
@Composable
private fun Preview() {
    TraktThemeLightDark {
        Column(
            verticalArrangement = spacedBy(10.dp),
            modifier = Modifier.padding(16.dp),
        ) {
            SharedByItemCard(
                user = PreviewData.user1,
                muting = false,
                containerColor = TraktTheme.colors.dialogOnContainer,
                modifier = Modifier.fillMaxWidth(),
            )
            SharedByItemCard(
                user = PreviewData.user1.copy(isVip = true),
                muting = false,
                containerColor = TraktTheme.colors.dialogOnContainer,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
