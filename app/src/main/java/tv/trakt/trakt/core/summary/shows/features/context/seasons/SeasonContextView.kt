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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.Season
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.buttons.GhostButton
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun SeasonContextView(
    show: Show,
    season: Season,
    watched: Boolean,
    modifier: Modifier = Modifier,
    onTrackClick: () -> Unit = {},
    onRemoveClick: () -> Unit = {},
    onListsClick: (() -> Unit)? = null,
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

            Text(
                text = show.title,
                color = TraktTheme.colors.textSecondary,
                style = TraktTheme.typography.paragraphSmall,
                maxLines = 1,
                overflow = Ellipsis,
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
                    text = stringResource(R.string.button_text_track),
                    icon = painterResource(R.drawable.ic_check_2),
                    iconSize = 21.dp,
                    iconSpace = 16.dp,
                    onClick = onTrackClick,
                    modifier = Modifier
                        .graphicsLayer {
                            translationX = -7.dp.toPx()
                        },
                )
            }

            if (onListsClick != null) {
                GhostButton(
                    text = stringResource(R.string.button_text_manage_lists),
                    icon = painterResource(R.drawable.ic_lists_off),
                    iconSize = 20.dp,
                    iconSpace = 17.dp,
                    onClick = onListsClick,
                    modifier = Modifier
                        .graphicsLayer {
                            translationX = -6.dp.toPx()
                        },
                )
            }
        }
    }
}

@PreviewTraktLightDark
@Composable
private fun Preview() {
    TraktThemePreview {
        SeasonContextView(
            show = PreviewData.show1,
            season = PreviewData.season1,
            watched = false,
            onListsClick = {},
        )
    }
}

@PreviewTraktLightDark
@Composable
private fun PreviewWatched() {
    TraktThemePreview {
        SeasonContextView(
            show = PreviewData.show1,
            season = PreviewData.season1,
            watched = true,
        )
    }
}
