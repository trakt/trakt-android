@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.summary.shows.features.context.episodes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.buttons.GhostButton
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun EpisodeContextView(
    episode: Episode,
    watched: Boolean,
    modifier: Modifier = Modifier,
    onTrackClick: () -> Unit = {},
    onWatchedUntilClick: () -> Unit = {},
    onRemoveClick: () -> Unit = {},
) {
    val released = episode.rememberReleased()

    Column(
        verticalArrangement = spacedBy(0.dp),
        modifier = modifier,
    ) {
        Column(
            verticalArrangement = spacedBy(2.dp),
        ) {
            Text(
                text = episode.title,
                color = TraktTheme.colors.textPrimary,
                style = TraktTheme.typography.heading3,
                maxLines = 1,
                overflow = Ellipsis,
                autoSize = TextAutoSize.StepBased(
                    maxFontSize = TraktTheme.typography.heading3.fontSize,
                    minFontSize = 20.sp,
                    stepSize = 2.sp,
                ),
            )

            Text(
                text = stringResource(
                    R.string.episode_footer_season_episode,
                    episode.season,
                    episode.number,
                ),
                color = TraktTheme.colors.textSecondary,
                style = TraktTheme.typography.paragraphSmall,
            )
        }

        Spacer(
            modifier = Modifier
                .padding(top = 22.dp)
                .background(TraktTheme.colors.separator)
                .fillMaxWidth()
                .height(1.dp),
        )

        Column(
            verticalArrangement = spacedBy(TraktTheme.spacing.contextItemsSpace),
            modifier = Modifier.padding(top = 14.dp),
        ) {
            if (watched) {
                GhostButton(
                    text = stringResource(R.string.button_text_remove_from_history),
                    icon = painterResource(R.drawable.ic_close),
                    iconSize = 22.dp,
                    iconSpace = 15.5.dp,
                    onClick = onRemoveClick,
                    modifier = Modifier
                        .graphicsLayer {
                            translationX = -6.dp.toPx()
                        },
                )
            } else {
                GhostButton(
                    enabled = released,
                    text = stringResource(R.string.button_text_track),
                    icon = painterResource(R.drawable.ic_check_2),
                    iconSize = 20.dp,
                    iconSpace = 16.dp,
                    onClick = onTrackClick,
                    modifier = Modifier
                        .graphicsLayer {
                            translationX = -7.dp.toPx()
                        },
                )

                GhostButton(
                    enabled = released,
                    text = stringResource(R.string.button_text_watched_until_here),
                    icon = painterResource(R.drawable.ic_check_2),
                    iconSize = 20.dp,
                    iconSpace = 16.dp,
                    onClick = onWatchedUntilClick,
                    modifier = Modifier
                        .graphicsLayer {
                            translationX = -7.dp.toPx()
                        },
                )
            }
        }
    }
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview() {
    TraktTheme {
        EpisodeContextView(
            episode = PreviewData.episode1,
            watched = false,
        )
    }
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun PreviewWatched() {
    TraktTheme {
        EpisodeContextView(
            episode = PreviewData.episode1,
            watched = true,
        )
    }
}
