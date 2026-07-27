package tv.trakt.trakt.core.profile.sections.activity.all.ui

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
import androidx.compose.ui.Alignment.Companion.Bottom
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight.Companion.W500
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tv.trakt.trakt.common.helpers.extensions.longDateTimeFormat
import tv.trakt.trakt.common.helpers.extensions.toLocal
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.Images.Size.THUMB
import tv.trakt.trakt.core.profile.sections.activity.model.ProfileRatingItem
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.mediacards.PanelMediaCard
import tv.trakt.trakt.ui.theme.TraktTheme
import java.time.Instant
import java.util.Locale

@Composable
internal fun ProfileAllActivitySeasonItem(
    item: ProfileRatingItem.SeasonItem,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    onShowClick: (() -> Unit)? = null,
) {
    PanelMediaCard(
        modifier = modifier,
        title = item.show.title,
        titleOriginal = null,
        subtitle = when {
            item.season.isSpecial -> stringResource(R.string.text_season_specials)
            else -> stringResource(R.string.text_season_number, item.season.number)
        },
        contentImageUrl = item.show.images?.getPosterUrl(),
        containerImageUrl = item.show.images?.getFanartUrl(THUMB),
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
                    val ratingText = remember(item.rating) {
                        "%.1f"
                            .format(Locale.US, item.rating / 2f)
                            .removeSuffix(".0")
                            .removeSuffix(",0")
                    }
                    Row(
                        verticalAlignment = CenterVertically,
                        horizontalArrangement = spacedBy(3.dp),
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
                            style = TraktTheme.typography.meta.copy(fontSize = 13.sp),
                        )
                    }
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
        ProfileAllActivitySeasonItem(
            item = ProfileRatingItem.SeasonItem(
                rating = 3,
                ratedAt = Instant.now(),
                show = PreviewData.show1,
                season = PreviewData.season1,
            ),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
