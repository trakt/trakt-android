package tv.trakt.trakt.core.home.sections.activity.all.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.helpers.preview.PreviewData
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun UserFilterChip(
    user: User,
    selected: Boolean,
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = spacedBy(0.dp),
        modifier = modifier
            .onClick(onClick)
            .height(28.dp)
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
                start = 4.dp,
                end = 12.dp,
            ),
    ) {
//        AnimatedVisibility(
//            visible = selected,
//            enter = fadeIn(tween(150)) + expandHorizontally(tween(150)),
//            exit = fadeOut(tween(150)) + shrinkHorizontally(tween(150)),
//        ) {
//
//        }

//        val borderColor = remember(user.isAnyVip) {
//            if (user.isAnyVip) Color.Red else Color.Transparent
//        }

        if (user.hasAvatar) {
            AsyncImage(
                model = user.images?.avatar?.full,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                error = painterResource(R.drawable.ic_person_placeholder),
                modifier = Modifier
                    .size(20.dp)
//                    .border(1.dp, borderColor, CircleShape)
                    .clip(CircleShape),
            )
        } else {
            Image(
                painter = painterResource(R.drawable.ic_person_placeholder),
                contentDescription = null,
                modifier = Modifier
                    .size(20.dp)
//                    .border(1.dp, borderColor, CircleShape)
                    .clip(CircleShape),
            )
        }

        Text(
            text = text,
            style = TraktTheme.typography.buttonTertiary,
            color = TraktTheme.colors.textPrimary,
            maxLines = 1,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(start = 6.dp),
        )
    }
}

@Preview
@Composable
private fun UserFilterChipPreview() {
    TraktTheme {
        UserFilterChip(
            user = PreviewData.user1,
            selected = false,
            text = PreviewData.user1.displayName,
        )
    }
}

