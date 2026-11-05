package tv.trakt.trakt.core.summary.soundtrack.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import kotlinx.coroutines.delay
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.core.summary.soundtrack.model.SoundtrackSummary
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme
import kotlin.time.Duration.Companion.milliseconds

private val PanelHeight = 80.dp
private val PanelShape = RoundedCornerShape(12.dp)
private val ArtworkShape = RoundedCornerShape(8.dp)

/** Spotify's card reflows after it reports ready, so the cover stays until playback reports in. */
private val SettleTimeout = 2500.milliseconds

@Composable
internal fun SoundtrackPanel(
    playback: SoundtrackPlayback,
    summary: SoundtrackSummary,
    title: String,
    posterUrl: String?,
    modifier: Modifier = Modifier,
) {
    val spotifyId = playback.selected?.spotifyId
    var embedVisible by remember { mutableStateOf(false) }

    LaunchedEffect(spotifyId) {
        embedVisible = false
        delay(SettleTimeout)
        embedVisible = true
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(PanelHeight)
            .clip(PanelShape),
    ) {
        if (spotifyId != null) {
            SpotifyEmbedPlayer(
                spotifyId = spotifyId,
                playToken = playback.playToken,
                onPlayingChange = {
                    embedVisible = true
                    playback.playing = it
                },
                modifier = Modifier.fillMaxSize(),
            )
        }

        AnimatedVisibility(
            visible = spotifyId == null || !embedVisible,
            enter = EnterTransition.None,
            exit = fadeOut(tween(150)),
        ) {
            SoundtrackPanelCover(
                title = summary.credit ?: title,
                subtitle = stringResource(
                    R.string.text_soundtrack_playable_ratio,
                    summary.playable,
                    summary.total,
                ),
                hint = if (spotifyId == null) stringResource(R.string.text_soundtrack_pick_a_track) else null,
                posterUrl = posterUrl,
            )
        }
    }
}

@Composable
private fun SoundtrackPanelCover(
    title: String,
    subtitle: String,
    hint: String?,
    posterUrl: String?,
) {
    Row(
        horizontalArrangement = spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxSize()
            .background(TraktTheme.colors.panelCardContainer)
            .padding(12.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .aspectRatio(1F)
                .clip(ArtworkShape)
                .background(TraktTheme.colors.placeholderContainer),
        ) {
            if (posterUrl != null) {
                AsyncImage(
                    model = posterUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

        Column(
            verticalArrangement = spacedBy(2.dp),
            modifier = Modifier.weight(1F),
        ) {
            Text(
                text = title,
                color = TraktTheme.colors.textPrimary,
                style = TraktTheme.typography.paragraphSmall.copy(fontWeight = FontWeight.W600),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = subtitle,
                color = TraktTheme.colors.textSecondary,
                style = TraktTheme.typography.meta,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            hint?.let {
                Text(
                    text = it,
                    color = TraktTheme.colors.textSecondary,
                    style = TraktTheme.typography.meta,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@PreviewTraktLightDark
@Composable
private fun Preview() {
    TraktThemePreview {
        SoundtrackPanel(
            playback = rememberSoundtrackPlayback(),
            summary = SoundtrackSummary(total = 24, playable = 15, credit = "Trent Reznor"),
            title = "The Social Network",
            posterUrl = null,
        )
    }
}
