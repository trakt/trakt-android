package tv.trakt.trakt.core.today.stories.ui

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.core.today.model.TodayForYouItem
import tv.trakt.trakt.core.today.model.TodayMedia
import tv.trakt.trakt.helpers.extensions.TraktThemeLightDark
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun TodayForYouFooter(
    item: TodayForYouItem,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = spacedBy(4.dp),
        modifier = modifier,
    ) {
        Text(
            text = stringResource(
                when (item) {
                    is TodayForYouItem.UpNext -> R.string.tag_text_today_new_episode
                    is TodayForYouItem.StartWatching -> R.string.tag_text_today_released
                },
            ).uppercase(),
            style = TraktTheme.typography.meta,
            color = when (TraktTheme.colors.isLight) {
                true -> TraktTheme.colors.accent
                false -> TraktTheme.colors.accentPalette.shade400
            },
            maxLines = 1,
        )

        Text(
            text = item.media.title,
            style = TraktTheme.typography.heading6,
            color = TraktTheme.colors.textPrimary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )

        if (item is TodayForYouItem.UpNext) {
            Text(
                text = item.episode.seasonEpisodeString(),
                style = TraktTheme.typography.cardSubtitle,
                color = TraktTheme.colors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFF131517,
    widthDp = 360,
)
@Composable
private fun Preview() {
    TraktThemeLightDark {
        Column(
            verticalArrangement = spacedBy(24.dp),
            modifier = Modifier.padding(16.dp),
        ) {
            TodayForYouFooter(
                item = TodayForYouItem.UpNext(
                    show = PreviewData.show1,
                    episode = PreviewData.episode1,
                    releasedAt = nowUtcInstant(),
                ),
            )
            TodayForYouFooter(
                item = TodayForYouItem.StartWatching(
                    media = TodayMedia.MovieMedia(PreviewData.movie1),
                    releasedAt = nowUtcInstant(),
                ),
            )
        }
    }
}
