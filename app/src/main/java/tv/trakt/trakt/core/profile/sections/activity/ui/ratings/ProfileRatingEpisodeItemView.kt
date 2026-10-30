package tv.trakt.trakt.core.profile.sections.activity.ui.ratings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.extensions.relativePastDateString
import tv.trakt.trakt.common.helpers.extensions.toLocal
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.profile.ProfileRatingItem
import tv.trakt.trakt.core.home.sections.activity.views.ActivityRateButton
import tv.trakt.trakt.ui.components.chips.InfoChip
import tv.trakt.trakt.ui.components.mediacards.HorizontalMediaCard
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun ProfileRatingEpisodeItemView(
    item: ProfileRatingItem.EpisodeItem,
    modifier: Modifier = Modifier,
    onClick: (TraktId) -> Unit = { },
    onShowClick: (TraktId) -> Unit = { },
    onRateClick: (Int?) -> Unit = {},
) {
    HorizontalMediaCard(
        modifier = modifier,
        title = "",
        more = false,
        onClick = { onClick(item.episode.ids.trakt) },
        containerImageUrl = item.episode.images?.getScreenshotUrl()
            ?: item.show.images?.getFanartUrl(),
        cardContent = {
            InfoChip(
                text = item.ratedAt.toLocal().relativePastDateString(),
                containerColor = TraktTheme.colors.chipContainerOnContent,
            )
        },
        footerContent = {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth(),
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(1.dp),
                    modifier = Modifier
                        .weight(1F, fill = false)
                        .onClick { onShowClick(item.show.ids.trakt) },
                ) {
                    Text(
                        text = item.show.title,
                        style = TraktTheme.typography.cardTitle,
                        color = TraktTheme.colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    Text(
                        text = item.episode.seasonEpisodeString(),
                        style = TraktTheme.typography.cardSubtitle,
                        color = TraktTheme.colors.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                val rating = remember(item) { item.userRating }
                ActivityRateButton(
                    rating = rating,
                    title = item.show.title,
                    onRateClick = onRateClick,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        },
    )
}
