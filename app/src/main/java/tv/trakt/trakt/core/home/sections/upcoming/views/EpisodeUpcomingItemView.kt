package tv.trakt.trakt.core.home.sections.upcoming.views

import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.persistentListOf
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.extensions.relativeDateTimeString
import tv.trakt.trakt.common.helpers.extensions.toLocal
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.core.calendar.model.CalendarItem
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.chips.FinaleChip
import tv.trakt.trakt.ui.components.chips.InfoChip
import tv.trakt.trakt.ui.components.chips.PremiereChip
import tv.trakt.trakt.ui.components.mediacards.HorizontalMediaCard
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun EpisodeUpcomingItemView(
    item: CalendarItem.EpisodeItem,
    modifier: Modifier = Modifier,
    midReleases: Boolean = false,
    onClick: () -> Unit,
    onShowClick: () -> Unit,
) {
    HorizontalMediaCard(
        title = "",
        more = false,
        onClick = onClick,
        containerImageUrl =
            item.show.images?.getFanartUrl()
                ?: item.episode.images?.getScreenshotUrl(),
        cardContent = {
            Row(
                horizontalArrangement = spacedBy(3.dp),
                verticalAlignment = CenterVertically,
            ) {
                val shadowModifier = Modifier.shadow(2.dp, RoundedCornerShape(100))
                when {
                    item.episode.isPremiere(isLatestAired = midReleases) -> PremiereChip(modifier = shadowModifier)
                    item.episode.isFinale(isLatestAired = midReleases) -> FinaleChip(modifier = shadowModifier)
                }

                InfoChip(
                    text = item.releasedAt?.toLocal()?.relativeDateTimeString() ?: "N/A",
                    iconPainter = painterResource(R.drawable.ic_calendar_upcoming),
                    containerColor = TraktTheme.colors.chipContainerOnContent,
                    modifier = shadowModifier,
                )
            }
        },
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
                    item.episodes.size > 1 -> stringResource(
                        R.string.episode_footer_season_episode_range,
                        item.episode.season,
                        item.episodes.first().number,
                        item.episodes.last().number,
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
        EpisodeUpcomingItemView(
            item = CalendarItem.EpisodeItem(
                show = PreviewData.show1,
                episodes = persistentListOf(PreviewData.episode1),
                watched = false,
            ),
            onClick = {},
            onShowClick = {},
        )
    }
}
