package tv.trakt.trakt.core.profile.sections.activity.all.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Bottom
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight.Companion.W500
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.helpers.extensions.longDateTimeFormat
import tv.trakt.trakt.common.helpers.extensions.toLocal
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.Images.Size.THUMB
import tv.trakt.trakt.common.model.profile.ProfileRatingItem
import tv.trakt.trakt.core.home.sections.activity.views.ActivityRateButton
import tv.trakt.trakt.ui.components.mediacards.PanelMediaCard
import tv.trakt.trakt.ui.theme.TraktTheme
import java.time.Instant

@Composable
internal fun ProfileAllActivityEpisodeItem(
    item: ProfileRatingItem.EpisodeItem,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    onShowClick: (() -> Unit)? = null,
    onRateClick: (Int?) -> Unit = {},
) {
    PanelMediaCard(
        modifier = modifier,
        title = item.show.title,
        titleOriginal = null,
        subtitle = item.episode.seasonEpisodeString(),
        contentImageUrl = item.show.images?.getPosterUrl(),
        containerImageUrl = item.episode.images?.getScreenshotUrl(THUMB)
            ?: item.episode.images?.getFanartUrl(THUMB),
        onClick = onClick,
        onImageClick = onShowClick,
        more = false,
        footerContent = {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Bottom,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    verticalAlignment = CenterVertically,
                    modifier = Modifier.weight(1F),
                ) {
                    Text(
                        text = item.ratedAt.toLocal().format(longDateTimeFormat()),
                        color = TraktTheme.colors.textPrimary,
                        style = TraktTheme.typography.cardSubtitle.copy(
                            fontWeight = W500,
                        ),
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = spacedBy(8.dp),
                    modifier = Modifier.padding(start = 12.dp),
                ) {
                    val rating = remember(item) { item.userRating }
                    ActivityRateButton(
                        rating = rating,
                        title = item.show.title,
                        onRateClick = onRateClick,
                        modifier = Modifier.offset(x = 4.dp, y = 4.dp),
                    )
                }
            }
        },
    )
}

@Preview(
    widthDp = 400,
)
@Composable
private fun Preview() {
    TraktTheme {
        ProfileAllActivityEpisodeItem(
            item = ProfileRatingItem.EpisodeItem(
                rating = 3,
                ratedAt = Instant.now(),
                show = PreviewData.show1,
                episode = PreviewData.episode1,
            ),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
