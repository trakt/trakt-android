package tv.trakt.trakt.ui.components.usertier

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.core.vipveteran.model.LocalUserTierState
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

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
    borderColor: Color = defaultAvatarBorderColor(user),
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

@Composable
private fun defaultAvatarBorderColor(user: User): Color {
    return when {
        user.isAnyVip -> TraktTheme.colors.vipAccent
        else -> Color.Transparent
    }
}
