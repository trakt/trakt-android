package tv.trakt.trakt.core.calendar.ui

import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.toTraktId
import tv.trakt.trakt.core.home.sections.upcoming.model.HomeUpcomingItem
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.mediacards.HorizontalMediaCard
import tv.trakt.trakt.ui.theme.TraktTheme
import java.time.Instant

@Composable
internal fun CalendarEpisodeItemView(
    item: HomeUpcomingItem.EpisodeItem,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onShowClick: () -> Unit,
) {
    HorizontalMediaCard(
        title = "",
        more = false,
        onClick = onClick,
        containerImageUrl =
            item.episode.images?.getScreenshotUrl()
                ?: item.show.images?.getFanartUrl(),
        footerContent = {
            Column(
                verticalArrangement = spacedBy(1.dp),
                modifier = Modifier
                    .onClick(onClick = onShowClick),
            ) {
                Text(
                    text = item.show.title,
                    style = TraktTheme.typography.cardTitle,
                    color = TraktTheme.colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                val subtitle = when {
                    item.isFullSeason -> stringResource(
                        R.string.text_season_number,
                        item.episode.season,
                    )

                    else -> item.episode.seasonEpisodeString()
                }

                Text(
                    text = subtitle,
                    style = TraktTheme.typography.cardSubtitle,
                    color = TraktTheme.colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        },
        modifier = modifier,
    )
}

@Preview
@Composable
private fun Preview() {
    TraktTheme {
        CalendarEpisodeItemView(
            item = HomeUpcomingItem.EpisodeItem(
                id = 1.toTraktId(),
                releasedAt = Instant.now(),
                show = PreviewData.show1,
                episode = PreviewData.episode1,
            ),
            onClick = {},
            onShowClick = {},
        )
    }
}
