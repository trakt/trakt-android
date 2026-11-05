package tv.trakt.trakt.core.summary.soundtrack.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.core.summary.soundtrack.model.SoundtrackTeaserTracks
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.vip.VipChip
import tv.trakt.trakt.ui.components.vip.onUpsellSeen
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun SoundtrackUpsell(
    modifier: Modifier = Modifier,
    onVipClick: () -> Unit = {},
    onVipSeen: () -> Unit = {},
) {
    Column(
        verticalArrangement = spacedBy(12.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        // Sample rows, not this title's data. Blurred so they read as a preview of the feature.
        SoundtrackTrackList(
            tracks = SoundtrackTeaserTracks,
            playback = rememberSoundtrackPlayback(),
            modifier = Modifier.blur(4.dp),
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = spacedBy(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .onUpsellSeen(onVipSeen)
                .background(TraktTheme.colors.panelCardContainer, SoundtrackListShape)
                .onClick(onClick = onVipClick)
                .padding(start = 16.dp, end = 14.dp)
                .padding(vertical = 12.dp),
        ) {
            Column(
                verticalArrangement = spacedBy(3.dp),
                modifier = Modifier.weight(1F),
            ) {
                Text(
                    text = stringResource(R.string.text_soundtrack_upsell),
                    color = TraktTheme.colors.textPrimary,
                    style = TraktTheme.typography.paragraphSmall.copy(fontWeight = FontWeight.W600),
                )
                Text(
                    text = stringResource(R.string.vip_feature_description_soundtrack),
                    color = TraktTheme.colors.textSecondary,
                    style = TraktTheme.typography.paragraphSmaller,
                )
            }

            VipChip(
                text = stringResource(R.string.badge_text_get_vip),
                color = TraktTheme.colors.accent,
                onClick = onVipClick,
            )
        }
    }
}

@PreviewTraktLightDark
@Composable
private fun Preview() {
    TraktThemePreview {
        SoundtrackUpsell()
    }
}
