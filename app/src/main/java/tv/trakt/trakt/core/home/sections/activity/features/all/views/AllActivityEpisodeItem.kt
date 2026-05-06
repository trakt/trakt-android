package tv.trakt.trakt.core.home.sections.activity.features.all.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.Bottom
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight.Companion.W500
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.helpers.extensions.relativePastDateString
import tv.trakt.trakt.common.helpers.extensions.toLocal
import tv.trakt.trakt.common.model.Images.Size.THUMB
import tv.trakt.trakt.common.model.ratings.UserRating
import tv.trakt.trakt.core.home.sections.activity.model.HomeActivityItem
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.mediacards.PanelMediaCard
import tv.trakt.trakt.ui.theme.TraktTheme
import java.time.format.DateTimeFormatter

@Composable
internal fun AllActivityEpisodeItem(
    item: HomeActivityItem.EpisodeItem,
    modifier: Modifier = Modifier,
    itemRating: UserRating? = null,
    moreButton: Boolean = false,
    dateFormat: DateTimeFormatter? = null,
    onClick: (() -> Unit)? = null,
    onShowClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
) {
    val isPast = remember(item.activityAt) {
        !item.activityAt.isAfter(nowUtcInstant())
    }

    PanelMediaCard(
        title = item.show.title,
        titleOriginal = null,
        subtitle = item.episode.seasonEpisodeString(),
        contentImageUrl = item.show.images?.getPosterUrl(),
        containerImageUrl = item.episode.images?.getScreenshotUrl(THUMB)
            ?: item.episode.images?.getFanartUrl(THUMB),
        onClick = onClick,
        onImageClick = onShowClick,
        onLongClick = onLongClick,
        more = moreButton,
        footerContent = {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Bottom,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    verticalAlignment = CenterVertically,
                    modifier = Modifier.alpha(if (isPast) 1.0f else 0f),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_calendar_check),
                        contentDescription = null,
                        tint = TraktTheme.colors.textPrimary,
                        modifier = Modifier.size(14.dp),
                    )
                    Text(
                        text = when (dateFormat) {
                            null -> item.activityAt.toLocal().relativePastDateString()
                            else -> item.activityAt.toLocal().format(dateFormat)
                        },
                        color = TraktTheme.colors.textPrimary,
                        style = TraktTheme.typography.cardSubtitle.copy(
                            fontWeight = W500,
                        ),
                    )
                }

                item.user?.let { user ->
                    AllActivityUserChip(
                        user = user,
                        modifier = Modifier.padding(start = 12.dp),
                    )
                }

                itemRating?.let {
                    Row(
                        verticalAlignment = CenterVertically,
                        horizontalArrangement = spacedBy(2.dp),
                        modifier = Modifier.padding(start = 12.dp),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_star_trakt_on),
                            contentDescription = null,
                            modifier = Modifier.size(13.dp),
                            tint = TraktTheme.colors.textPrimary,
                        )
                        Text(
                            text = it.rating5Scale,
                            color = TraktTheme.colors.textPrimary,
                            style = TraktTheme.typography.meta.copy(fontSize = 12.sp),
                        )
                    }
                }
            }
        },
        modifier = modifier,
    )
}
