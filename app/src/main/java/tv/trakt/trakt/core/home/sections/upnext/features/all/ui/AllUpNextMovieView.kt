package tv.trakt.trakt.core.home.sections.upnext.features.all.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.core.home.model.UpNextMovie
import tv.trakt.trakt.common.helpers.extensions.rememberDurationFormat
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.EpisodeProgressBar
import tv.trakt.trakt.ui.components.mediacards.PanelMediaCard
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun AllUpNextMovieView(
    item: UpNextMovie,
    enabled: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onMovieClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PanelMediaCard(
        enabled = enabled,
        title = item.movie.title,
        titleOriginal = item.movie.titleOriginal,
        subtitle = rememberDurationFormat(item.movie.runtime?.inWholeMinutes),
        contentImageUrl = item.movie.images?.getPosterUrl(),
        containerImageUrl = item.movie.images?.getFanartUrl(Images.Size.THUMB),
        onClick = onClick,
        onLongClick = onLongClick,
        onImageClick = onMovieClick,
        footerContent = {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                val remainingPercent = remember(item.progress.progress) {
                    (100F - item.progress.progress) / 100F
                }

                Column(
                    verticalArrangement = Arrangement.Absolute.spacedBy(4.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        EpisodeProgressBar(
                            startText = stringResource(
                                R.string.tag_text_remaining_duration,
                                item.remainingTimeText() ?: "?",
                            ),
                            progress = remainingPercent,
                            containerColor = TraktTheme.colors.chipContainer,
                            modifier = Modifier.weight(1F, fill = false),
                        )
                    }
                }
            }
        },
        modifier = modifier
            .padding(bottom = TraktTheme.spacing.mainListVerticalSpace),
    )
}
