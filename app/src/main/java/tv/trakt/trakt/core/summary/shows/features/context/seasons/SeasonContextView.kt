package tv.trakt.trakt.core.summary.shows.features.context.seasons

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.TextAutoSize
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
import tv.trakt.trakt.common.model.Season
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.buttons.GhostButton
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun SeasonContextView(
    season: Season,
    watched: Boolean,
    watchOnlyOnce: Boolean?,
    modifier: Modifier = Modifier,
    showTitle: String? = null,
    onTrackClick: () -> Unit = {},
    onRemoveClick: () -> Unit = {},
) {
    Column(
        verticalArrangement = spacedBy(0.dp),
        modifier = modifier,
    ) {
        Column(
            verticalArrangement = spacedBy(2.dp),
        ) {
            Text(
                text = when {
                    season.isSpecial -> stringResource(R.string.text_season_specials)
                    else -> stringResource(R.string.text_season_number, season.number)
                },
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

            showTitle?.let {
                Text(
                    text = it,
                    color = TraktTheme.colors.textSecondary,
                    style = TraktTheme.typography.paragraphSmall,
                    maxLines = 1,
                    overflow = Ellipsis,
                )
            }
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
            if (!watched || watchOnlyOnce != true) {
                GhostButton(
                    text = when {
                        !watched -> stringResource(R.string.button_text_track)
                        else -> stringResource(R.string.button_text_watch_again)
                    },
                    icon = when {
                        !watched -> painterResource(R.drawable.ic_check_2)
                        else -> painterResource(R.drawable.ic_check_double)
                    },
                    iconSize = 22.dp,
                    iconSpace = 14.dp,
                    onClick = onTrackClick,
                    modifier = Modifier
                        .graphicsLayer {
                            translationX = -4.dp.toPx()
                        },
                )
            }

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
        SeasonContextView(
            season = PreviewData.season1,
            watched = false,
            watchOnlyOnce = false,
            showTitle = PreviewData.show1.title,
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
        SeasonContextView(
            season = PreviewData.season1,
            watched = true,
            watchOnlyOnce = false,
            showTitle = PreviewData.show1.title,
        )
    }
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun PreviewWatchedOnlyOnce() {
    TraktTheme {
        SeasonContextView(
            season = PreviewData.season1,
            watched = true,
            watchOnlyOnce = true,
            showTitle = PreviewData.show1.title,
        )
    }
}
