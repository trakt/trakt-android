package tv.trakt.trakt.core.profile.sections.activity.ui.ratings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.extensions.relativePastDateString
import tv.trakt.trakt.common.helpers.extensions.toLocal
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.profile.sections.activity.model.ProfileRatingItem
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.chips.InfoChip
import tv.trakt.trakt.ui.components.mediacards.HorizontalMediaCard
import tv.trakt.trakt.ui.theme.TraktTheme
import java.util.Locale

@Composable
internal fun ProfileRatingSeasonItemView(
    item: ProfileRatingItem.SeasonItem,
    modifier: Modifier = Modifier,
    onShowClick: (TraktId) -> Unit = { },
) {
    HorizontalMediaCard(
        modifier = modifier,
        title = "",
        more = false,
        onClick = { onShowClick(item.show.ids.trakt) },
        containerImageUrl = item.show.images?.getFanartUrl(),
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
                        text = when {
                            item.season.isSpecial -> stringResource(R.string.text_season_specials)
                            else -> stringResource(R.string.text_season_number, item.season.number)
                        },
                        style = TraktTheme.typography.cardSubtitle,
                        color = TraktTheme.colors.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                val ratingText = remember(item.rating) {
                    "%.1f"
                        .format(Locale.US, item.rating / 2f)
                        .removeSuffix(".0")
                        .removeSuffix(",0")
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = spacedBy(3.dp),
                    modifier = Modifier.padding(start = 12.dp, end = 1.dp),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_star_trakt_on),
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = TraktTheme.colors.textPrimary,
                    )
                    Text(
                        text = ratingText,
                        color = TraktTheme.colors.textPrimary,
                        style = TraktTheme.typography.meta.copy(fontSize = 12.sp),
                    )
                }
            }
        },
    )
}
