package tv.trakt.trakt.core.home.sections.activity.features.all.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.chips.InfoChip
import tv.trakt.trakt.ui.components.usertier.UserAvatar
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun AllActivityUserChip(
    user: User,
    modifier: Modifier = Modifier,
    onUserClick: () -> Unit,
) {
    Box(
        contentAlignment = Alignment.CenterEnd,
        modifier = modifier
            .sizeIn(maxHeight = 26.dp)
            .onClick(onClick = onUserClick),
    ) {
        InfoChip(
            text = " ${user.displayName}",
            containerColor = TraktTheme.colors.chipContainerOnContent,
            endPadding = 26.dp,
            modifier = Modifier.fillMaxHeight(),
        )
        UserAvatar(
            user = user,
            borderWidth = 1.5.dp,
            modifier = Modifier.size(26.dp),
        )
    }
}
