package tv.trakt.trakt.core.home.sections.activity.all.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.Bottom
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tv.trakt.trakt.common.helpers.extensions.relativePastDateString
import tv.trakt.trakt.common.helpers.extensions.toLocal
import tv.trakt.trakt.common.model.Images.Size.THUMB
import tv.trakt.trakt.core.home.sections.activity.model.HomeActivityItem
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.mediacards.PanelMediaCard
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun AllActivityEpisodeItem(
    item: HomeActivityItem.EpisodeItem,
    modifier: Modifier = Modifier,
    moreButton: Boolean = false,
    onClick: (() -> Unit)? = null,
    onShowClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
) {
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
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_calendar_check),
                        contentDescription = null,
                        tint = TraktTheme.colors.textSecondary,
                        modifier = Modifier.Companion.size(14.dp),
                    )
                    Text(
                        text = item.activityAt.toLocal().relativePastDateString(),
                        color = TraktTheme.colors.textSecondary,
                        style = TraktTheme.typography.meta.copy(fontSize = 12.sp),
                    )
                }

                item.user?.let { user ->
                    AllActivityUserChip(
                        user = user,
                        modifier = Modifier.padding(start = 12.dp),
                    )
                }
            }
        },
        modifier = modifier,
    )
}
