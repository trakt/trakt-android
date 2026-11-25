package tv.trakt.trakt.ui.components.usertier

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.UserTier
import tv.trakt.trakt.common.model.VipVeteran
import tv.trakt.trakt.core.vipveteran.model.LocalUserTierState
import tv.trakt.trakt.core.vipveteran.model.UserTierState
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme
import java.time.Instant

internal enum class AvatarVariant(
    private val segment: String?,
) {
    Full(null),
    Medium("medium"),
    Thumb("thumb"),
    ;

    fun resolve(url: String): String {
        if (segment == null) return url

        return when {
            url.startsWith("http") -> url
            else -> "https://$url"
        }
            .replace("/medium/", "/$segment/")
            .replace("/original/", "/$segment/")
    }
}

@Composable
internal fun UserAvatar(
    user: User,
    modifier: Modifier = Modifier,
    borderWidth: Dp = 2.dp,
    borderColor: Color = when {
        user.isAnyVip -> TraktTheme.colors.vipAccent
        else -> Color.Transparent
    },
    animated: Boolean = false,
    ringEnabled: Boolean = true,
    variant: AvatarVariant = AvatarVariant.Full,
    contentDescription: String? = null,
) {
    val imageUrl = user.images?.avatar?.full
    val imageModel = remember(imageUrl, variant) { imageUrl?.let(variant::resolve) }
    val tier = if (ringEnabled) LocalUserTierState.current.tierOf(user) else null
    val avatarModifier = modifier
        .then(
            when (tier) {
                null -> Modifier.border(borderWidth, borderColor, CircleShape)
                else -> Modifier.userTierRing(tier, animated)
            },
        )
        .clip(CircleShape)

    if (imageModel != null) {
        AsyncImage(
            model = imageModel,
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            placeholder = painterResource(R.drawable.ic_person_placeholder),
            error = painterResource(R.drawable.ic_person_placeholder),
            modifier = avatarModifier,
        )
    } else {
        Image(
            painter = painterResource(R.drawable.ic_person_placeholder),
            contentDescription = contentDescription,
            modifier = avatarModifier,
        )
    }
}

private fun previewUser(tier: UserTier): User {
    val title = when (tier) {
        UserTier.Pro -> VipVeteran.Title.Pro
        UserTier.Veteran -> VipVeteran.Title.Veteran
        UserTier.Elite -> VipVeteran.Title.Elite
        UserTier.Legend -> VipVeteran.Title.Legend
        else -> null
    }
    return PreviewData.user1.copy(
        isVip = tier != UserTier.Free,
        veteran = title?.let {
            VipVeteran(
                since = Instant.EPOCH,
                years = 1,
                title = it,
                graceEndsAt = null,
            )
        },
    )
}

@PreviewTraktLightDark
@Composable
private fun TierRingsPreview() {
    TraktThemePreview {
        CompositionLocalProvider(LocalUserTierState provides UserTierState(enabled = true)) {
            Column(
                verticalArrangement = spacedBy(16.dp),
                modifier = Modifier.padding(16.dp),
            ) {
                listOf(24.dp, 36.dp, 52.dp).forEach { size ->
                    Row(horizontalArrangement = spacedBy(8.dp)) {
                        UserTier.entries.forEach { tier ->
                            UserAvatar(
                                user = previewUser(tier),
                                modifier = Modifier.size(size),
                            )
                        }
                    }
                }
            }
        }
    }
}

@PreviewTraktLightDark
@Composable
private fun LegacyBorderPreview() {
    TraktThemePreview {
        Row(
            horizontalArrangement = spacedBy(12.dp),
            modifier = Modifier.padding(16.dp),
        ) {
            UserAvatar(
                user = previewUser(UserTier.Free),
                modifier = Modifier.size(64.dp),
            )
            UserAvatar(
                user = previewUser(UserTier.Vip),
                modifier = Modifier.size(64.dp),
            )
            UserAvatar(
                user = previewUser(UserTier.Vip),
                borderWidth = 3.dp,
                borderColor = Color.White,
                modifier = Modifier.size(64.dp),
            )
        }
    }
}

@PreviewTraktLightDark
@Composable
private fun AnimatedRingPreview() {
    TraktThemePreview {
        CompositionLocalProvider(LocalUserTierState provides UserTierState(enabled = true)) {
            Row(
                horizontalArrangement = spacedBy(12.dp),
                modifier = Modifier.padding(16.dp),
            ) {
                UserAvatar(
                    user = previewUser(UserTier.Legend),
                    animated = true,
                    modifier = Modifier.size(96.dp),
                )
                UserAvatar(
                    user = previewUser(UserTier.Free),
                    ringEnabled = false,
                    modifier = Modifier.size(96.dp),
                )
            }
        }
    }
}
