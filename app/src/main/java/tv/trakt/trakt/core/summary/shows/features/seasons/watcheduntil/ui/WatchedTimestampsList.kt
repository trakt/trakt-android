package tv.trakt.trakt.core.summary.shows.features.seasons.watcheduntil.ui

import androidx.compose.foundation.layout.Arrangement.SpaceBetween
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight.Companion.W400
import androidx.compose.ui.text.font.FontWeight.Companion.W600
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import tv.trakt.trakt.common.helpers.extensions.longDateFormat
import tv.trakt.trakt.common.helpers.extensions.timeFormat
import tv.trakt.trakt.common.helpers.extensions.toLocal
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.core.summary.shows.features.seasons.watcheduntil.OtherDateBound
import tv.trakt.trakt.core.summary.shows.features.seasons.watcheduntil.WatchedUntilAction
import tv.trakt.trakt.core.summary.shows.features.seasons.watcheduntil.WatchedUntilAction.Now
import tv.trakt.trakt.core.summary.shows.features.seasons.watcheduntil.helpers.computeWatchedTimestamps
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme
import java.time.Instant

private val TimestampsMaxHeight = 250.dp

@Composable
internal fun WatchedTimestampsList(
    episodes: ImmutableList<Episode>,
    selectedAction: WatchedUntilAction,
    otherBound: OtherDateBound?,
    otherAnchor: Instant?,
    modifier: Modifier = Modifier,
) {
    val timestamps = remember(episodes, selectedAction, otherBound, otherAnchor) {
        computeWatchedTimestamps(episodes, selectedAction, otherBound, otherAnchor)
    }

    Column(
        verticalArrangement = spacedBy(8.dp),
        modifier = modifier,
    ) {
        Text(
            text = stringResource(R.string.text_watch_until_here_preview_title),
            style = TraktTheme.typography.meta.copy(fontWeight = W400),
            color = TraktTheme.colors.textSecondary,
            modifier = Modifier.padding(bottom = 4.dp),
        )

        Column(
            verticalArrangement = spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = TimestampsMaxHeight)
                .verticalScroll(rememberScrollState()),
        ) {
            episodes.forEachIndexed { index, episode ->
                EpisodeRow(
                    episode = episode,
                    timestamp = timestamps.getOrNull(index),
                )
            }
        }
    }
}

@Composable
private fun EpisodeRow(
    episode: Episode,
    timestamp: Instant?,
    modifier: Modifier = Modifier,
) {
    val dateFormat = longDateFormat()
    val timeFormat = timeFormat()

    val timestampText = timestamp?.toLocal()?.let {
        "${it.format(dateFormat)}  •  ${it.format(timeFormat)}"
    } ?: "-"

    Row(
        horizontalArrangement = SpaceBetween,
        verticalAlignment = CenterVertically,
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(
            text = stringResource(
                R.string.episode_footer_season_episode,
                episode.season,
                episode.number,
            ),
            style = TraktTheme.typography.buttonPrimary.copy(
                fontWeight = W600,
            ),
            color = TraktTheme.colors.textPrimary,
        )

        Text(
            text = timestampText,
            style = TraktTheme.typography.meta,
            color = TraktTheme.colors.textSecondary,
        )
    }
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF212427,
)
@Composable
private fun Preview() {
    TraktTheme {
        WatchedTimestampsList(
            episodes = persistentListOf(
                PreviewData.episode1,
                PreviewData.episode1.copy(number = 4),
                PreviewData.episode1.copy(number = 5),
            ),
            selectedAction = Now,
            otherBound = null,
            otherAnchor = null,
        )
    }
}
