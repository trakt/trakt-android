package tv.trakt.trakt.core.streamings.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.helpers.extensions.DevicePreview
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.model.streamings.YouTubeSpecial
import tv.trakt.trakt.helpers.extensions.TraktThemeLightDark
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun AllStreamingsYouTubeSpecialItem(
    special: YouTubeSpecial,
    modifier: Modifier = Modifier,
    onClick: (YouTubeSpecial) -> Unit = {},
) {
    Row(
        verticalAlignment = CenterVertically,
        horizontalArrangement = spacedBy(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .shadow(TraktTheme.colors.shadowDynamicSmall, GroupShape)
            .background(TraktTheme.colors.panelCardContainer, GroupShape)
            .then(
                when {
                    special.isOfficial -> Modifier.border(1.5.dp, TraktTheme.colors.accent, GroupShape)
                    else -> Modifier
                },
            )
            .onClick { onClick(special) }
            .padding(InnerSpace),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .height(LogoTileSize)
                .aspectRatio(5 / 4F)
                .background(TraktTheme.colors.placeholderContainer, ChipShape)
                .padding(horizontal = 8.dp),
        ) {
            Image(
                painter = painterResource(R.drawable.ic_youtube_full),
                contentDescription = null,
                contentScale = ContentScale.FillWidth,
                colorFilter = ColorFilter.tint(TraktTheme.colors.textPrimary),
                modifier = Modifier.height(18.dp),
            )
        }

        Column(
            verticalArrangement = spacedBy(1.dp),
            modifier = Modifier
                .weight(1F)
                .padding(end = 12.dp),
        ) {
            Text(
                text = stringResource(R.string.link_label_youtube),
                color = TraktTheme.colors.textPrimary,
                style = TraktTheme.typography.heading6,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Text(
                text = stringResource(
                    when {
                        special.isOfficial -> R.string.button_label_watch_official_upload_on_youtube
                        else -> R.string.button_label_watch_fan_upload_on_youtube
                    },
                ),
                color = TraktTheme.colors.textSecondary,
                style = TraktTheme.typography.cardSubtitle,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@DevicePreview
@Composable
private fun Preview() {
    TraktThemeLightDark {
        Column(
            verticalArrangement = spacedBy(12.dp),
            modifier = Modifier.padding(16.dp),
        ) {
            AllStreamingsYouTubeSpecialItem(
                special = YouTubeSpecial(videoId = "abc", isOfficial = true),
            )
            AllStreamingsYouTubeSpecialItem(
                special = YouTubeSpecial(videoId = "abc", isOfficial = false),
            )
        }
    }
}
