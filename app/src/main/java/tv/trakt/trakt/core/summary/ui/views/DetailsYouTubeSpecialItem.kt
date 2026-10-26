package tv.trakt.trakt.core.summary.ui.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.model.streamings.YouTubeSpecial
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun DetailsYouTubeSpecialItem(
    special: YouTubeSpecial,
    onClick: (YouTubeSpecial) -> Unit,
) {
    val itemShape = RoundedCornerShape(16.dp)
    val description = stringResource(
        when {
            special.isOfficial -> R.string.button_label_watch_official_upload_on_youtube
            else -> R.string.button_label_watch_fan_upload_on_youtube
        },
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = spacedBy(4.dp, CenterVertically),
        modifier = Modifier
            .height(86.dp)
            .shadow(
                elevation = TraktTheme.colors.shadowDynamicDefault,
                shape = itemShape,
                clip = false,
            )
            .background(TraktTheme.colors.commentContainer, itemShape)
            .then(
                when {
                    special.isOfficial -> Modifier.border(1.dp, TraktTheme.colors.accent, itemShape)
                    else -> Modifier
                },
            )
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .semantics { contentDescription = description }
            .onClick { onClick(special) },
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.height(52.dp),
        ) {
            Image(
                painter = painterResource(R.drawable.ic_youtube_full),
                contentDescription = null,
                contentScale = ContentScale.FillHeight,
                modifier = Modifier.height(24.dp),
            )
        }

        Row(
            verticalAlignment = CenterVertically,
            horizontalArrangement = spacedBy(6.dp),
        ) {
            Text(
                text = stringResource(R.string.list_title_streaming_free).uppercase(),
                color = TraktTheme.colors.textSecondary,
                style = TraktTheme.typography.meta,
            )

            if (!special.isOfficial) {
                Text(
                    text = stringResource(R.string.tag_label_fan_upload),
                    color = TraktTheme.colors.tagChipContent,
                    style = TraktTheme.typography.meta,
                    maxLines = 1,
                    modifier = Modifier
                        .background(TraktTheme.colors.tagChipContainer, RoundedCornerShape(100))
                        .padding(horizontal = 6.dp, vertical = 1.dp),
                )
            }
        }
    }
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview() {
    TraktTheme {
        Row(horizontalArrangement = spacedBy(12.dp)) {
            DetailsYouTubeSpecialItem(
                special = YouTubeSpecial(videoId = "abc", isOfficial = true),
                onClick = {},
            )
            DetailsYouTubeSpecialItem(
                special = YouTubeSpecial(videoId = "abc", isOfficial = false),
                onClick = {},
            )
        }
    }
}
