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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight.Companion.W500
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.helpers.extensions.relativePastDateString
import tv.trakt.trakt.common.helpers.extensions.toLocal
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.core.home.sections.activity.model.HomeActivityItem
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.mediacards.PanelMediaCard
import tv.trakt.trakt.ui.theme.TraktTheme
import java.time.Instant

@Composable
internal fun AllActivityMovieItem(
    item: HomeActivityItem.MovieItem,
    modifier: Modifier = Modifier,
    moreButton: Boolean = false,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
) {
    PanelMediaCard(
        title = item.title,
        titleOriginal = null,
        subtitle = stringResource(R.string.translated_value_type_movie),
        contentImageUrl = item.movie.images?.getPosterUrl(),
        containerImageUrl = item.movie.images?.getFanartUrl(Images.Size.THUMB),
        onClick = onClick,
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
                        tint = TraktTheme.colors.textPrimary,
                        modifier = Modifier.Companion.size(14.dp),
                    )
                    Text(
                        text = item.activityAt.toLocal().relativePastDateString(),
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
            }
        },
        modifier = modifier,
    )
}

@Preview(
    widthDp = 400,
)
@Composable
private fun AllActivityMovieItemPreview() {
    TraktTheme {
        AllActivityMovieItem(
            item = HomeActivityItem.MovieItem(
                id = 1L,
                activity = "watched",
                activityAt = Instant.now(),
                user = PreviewData.user1,
                movie = PreviewData.movie1,
            ),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
