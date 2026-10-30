package tv.trakt.trakt.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.core.userprofile.sections.match.ui.UserMatchPill
import tv.trakt.trakt.core.userprofile.sections.match.ui.UserMatchPreviewData
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

internal val AvatarSize = 64.dp

private val VipBadgeShine = Brush.linearGradient(
    0F to Color.White.copy(alpha = 0.3F),
    0.6F to Color.Transparent,
    1F to Color.Black.copy(alpha = 0.12F),
)

@Composable
internal fun UserHeaderRow(
    user: User,
    modifier: Modifier = Modifier,
    avatarBadge: (@Composable BoxScope.() -> Unit)? = null,
    trailing: @Composable () -> Unit = {},
) {
    Row(
        verticalAlignment = CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = CenterVertically,
            horizontalArrangement = spacedBy(12.dp),
            modifier = Modifier
                .weight(1F, false)
                .padding(end = 16.dp),
        ) {
            Box(
                contentAlignment = Alignment.BottomCenter,
            ) {
                UserAvatar(user = user)
                when {
                    avatarBadge != null -> avatarBadge()
                    user.isAnyVip -> VipAvatarBadge()
                }
            }

            TraktHeader(
                title = user.displayName,
                subtitle = remember(user) {
                    user.location?.takeIf { it.isNotBlank() }
                },
            )
        }

        trailing()
    }
}

@Composable
private fun UserAvatar(user: User) {
    val vipAccent = TraktTheme.colors.vipAccent
    val borderColor = remember(user, vipAccent) {
        when (user.isAnyVip) {
            true -> vipAccent
            else -> Color.White
        }
    }

    val avatarModifier = Modifier
        .size(AvatarSize)
        .border(3.dp, borderColor, CircleShape)
        .clip(CircleShape)

    if (user.hasAvatar) {
        AsyncImage(
            model = user.images?.avatar?.full,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            error = painterResource(R.drawable.ic_person_placeholder),
            modifier = avatarModifier,
        )
    } else {
        Image(
            painter = painterResource(R.drawable.ic_person_placeholder),
            contentDescription = null,
            modifier = avatarModifier,
        )
    }
}

@Composable
private fun VipAvatarBadge() {
    Row(
        verticalAlignment = CenterVertically,
        horizontalArrangement = spacedBy(2.dp, Alignment.CenterHorizontally),
        modifier = Modifier
            .widthIn(min = 36.dp, max = AvatarSize)
            .graphicsLayer {
                translationY = size.height / 8
            }
            .shadow(
                elevation = TraktTheme.colors.shadowDynamicSmall,
                shape = RoundedCornerShape(100),
            )
            .background(
                TraktTheme.colors.vipAccent,
                RoundedCornerShape(100),
            )
            .background(
                VipBadgeShine,
                RoundedCornerShape(100),
            )
            .padding(horizontal = 6.dp, vertical = 3.dp),
    ) {
        Text(
            text = stringResource(R.string.tag_text_vip).uppercase(),
            color = TraktTheme.colors.textPrimaryOnAccent,
            style = TraktTheme.typography.meta.copy(
                fontSize = 12.sp,
                letterSpacing = 0.03.sp,
            ),
            maxLines = 1,
            overflow = Ellipsis,
            autoSize = TextAutoSize.StepBased(
                maxFontSize = 12.sp,
                minFontSize = 8.sp,
                stepSize = 0.5.sp,
            ),
            modifier = Modifier.weight(1F, false),
        )
    }
}

@PreviewTraktLightDark
@Composable
private fun Preview() {
    TraktThemePreview {
        UserHeaderRow(
            user = PreviewData.user1.copy(
                location = "Some Location",
            ),
        )
    }
}

@PreviewTraktLightDark
@Composable
private fun PreviewVip() {
    TraktThemePreview {
        UserHeaderRow(
            user = PreviewData.user1.copy(
                location = "Some Location",
                isVip = true,
            ),
        )
    }
}

@PreviewTraktLightDark
@Composable
private fun PreviewPill() {
    TraktThemePreview {
        UserHeaderRow(
            user = PreviewData.user1.copy(
                location = "Some Location",
            ),
            trailing = {
                UserMatchPill(
                    match = UserMatchPreviewData.match,
                )
            },
        )
    }
}
