package tv.trakt.trakt.core.summary.ui.header.poster

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import tv.trakt.trakt.common.Config.webImdbPersonUrl
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.extensions.openExternalAppLink
import tv.trakt.trakt.common.model.ImdbId
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun PosterChipsGroup(
    watched: Boolean,
    watching: Boolean,
    creditsCount: Int,
    playsCount: Int,
    personImdb: ImdbId?,
    onWatchedChipClick: () -> Unit = {},
) {
    Row(
        horizontalArrangement = spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (creditsCount > 0) {
            PosterChip(
                text = "${stringResource(R.string.header_post_credits)} • $creditsCount".uppercase(),
            )
        }

        if (watched || watching) {
            PosterChip(
                text = when {
                    watching -> stringResource(R.string.tag_text_started)
                    playsCount > 1 -> "${stringResource(R.string.tag_text_watched)} • $playsCount"
                    else -> stringResource(R.string.tag_text_watched)
                },
                icon = painterResource(R.drawable.ic_check_double),
                modifier = Modifier.onClick(onClick = onWatchedChipClick),
            )
        }

        if (personImdb != null) {
            val context = LocalContext.current
            Image(
                painter = painterResource(R.drawable.ic_imdb_color),
                contentDescription = null,
                modifier = Modifier
                    .height(22.dp)
                    .graphicsLayer {
                        translationY = 1.dp.toPx()
                    }
                    .onClick {
                        openExternalAppLink(
                            context = context,
                            packageId = "com.imdb.mobile",
                            packageName = "imdb",
                            uri = webImdbPersonUrl(personImdb.value).toUri(),
                        )
                    },
            )
        }
    }
}

@Composable
private fun PosterChip(
    modifier: Modifier = Modifier,
    text: String,
    icon: Painter? = null,
) {
    val shape = RoundedCornerShape(100)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = spacedBy(6.dp),
        modifier = modifier
            .clip(shape)
            .background(TraktTheme.colors.tagChipContainer)
            .padding(
                horizontal = 8.dp,
                vertical = 4.dp,
            ),
    ) {
        icon?.let {
            Icon(
                painter = it,
                tint = TraktTheme.colors.tagChipContent,
                contentDescription = null,
                modifier = Modifier.size(13.dp),
            )
        }
        if (text.isNotBlank()) {
            Text(
                text = text.uppercase(),
                color = TraktTheme.colors.tagChipContent,
                style = TraktTheme.typography.meta,
            )
        }
    }
}

@PreviewTraktLightDark
@Composable
private fun Preview() {
    TraktThemePreview {
        PosterChipsGroup(
            watched = true,
            watching = false,
            creditsCount = 2,
            playsCount = 3,
            personImdb = null,
        )
    }
}
