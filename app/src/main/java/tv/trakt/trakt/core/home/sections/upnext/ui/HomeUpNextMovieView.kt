package tv.trakt.trakt.core.home.sections.upnext.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.extensions.rememberDurationFormat
import tv.trakt.trakt.core.home.sections.upnext.model.UpNextMovie
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.EpisodeProgressBar
import tv.trakt.trakt.ui.components.mediacards.HorizontalMediaCard
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun HomeUpNextMovieView(
    item: UpNextMovie,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onMovieClick: () -> Unit,
) {
    HorizontalMediaCard(
        title = "",
        onClick = onClick,
        onLongClick = onLongClick,
        containerImageUrl = item.movie.images?.getFanartUrl(),
        cardContent = {
            Row {
                val remainingPercent = remember(item.progress.progress) {
                    (100F - item.progress.progress) / 100F
                }

                EpisodeProgressBar(
                    startText = stringResource(R.string.tag_text_remaining_duration, item.remainingTimeText() ?: "?"),
                    containerColor = TraktTheme.colors.chipContainer.copy(alpha = 0.7F),
                    progress = (1F - remainingPercent).coerceIn(0F, 0.99F),
                    modifier = Modifier
                        .shadow(2.dp, RoundedCornerShape(100)),
                )
            }
        },
        footerContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(1.dp),
                    modifier = Modifier
                        .onClick(onClick = onMovieClick)
                        .weight(1F, fill = false),
                ) {
                    Text(
                        text = item.movie.title,
                        style = TraktTheme.typography.cardTitle,
                        color = TraktTheme.colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    Text(
                        text = rememberDurationFormat(item.movie.runtime?.inWholeMinutes),
                        style = TraktTheme.typography.cardSubtitle,
                        color = TraktTheme.colors.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        },
        modifier = modifier,
    )
}
