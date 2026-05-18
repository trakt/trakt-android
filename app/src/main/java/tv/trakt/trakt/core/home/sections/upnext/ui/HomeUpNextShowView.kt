package tv.trakt.trakt.core.home.sections.upnext.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.extensions.onClickCombined
import tv.trakt.trakt.common.helpers.extensions.rememberDurationFormat
import tv.trakt.trakt.common.ui.composables.FilmProgressIndicator
import tv.trakt.trakt.core.home.sections.upnext.model.UpNextShow
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.EpisodeProgressBar
import tv.trakt.trakt.ui.components.chips.FinaleChip
import tv.trakt.trakt.ui.components.chips.PremiereChip
import tv.trakt.trakt.ui.components.mediacards.HorizontalMediaCard
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun HomeUpNextShowView(
    item: UpNextShow,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onCheckClick: () -> Unit,
    onCheckLongClick: () -> Unit,
    onShowClick: () -> Unit,
) {
    HorizontalMediaCard(
        title = "",
        onClick = onClick,
        onLongClick = onLongClick,
        containerImageUrl =
            item.show.images?.getFanartUrl()
                ?: item.progress.nextEpisode?.images?.getScreenshotUrl(),
        cardContent = {
            Row {
                val runtime = item.progress.nextEpisode?.runtime?.inWholeMinutes
                    ?: item.show.runtime?.inWholeMinutes

                val remainingEpisodes = remember(item.progress.completed, item.progress.aired) {
                    item.progress.remainingEpisodes
                }

                val remainingPercent = remember(item.progress.completed, item.progress.aired) {
                    item.progress.remainingPercent
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    when {
                        item.progress.nextEpisode?.isPremiere(item.progress.isLatestAired) == true -> PremiereChip(
                            contentTextStyle = TraktTheme.typography.meta.copy(
                                fontSize = 10.sp,
                            ),
                            modifier = Modifier
                                .shadow(2.dp, RoundedCornerShape(100))
                                .height(20.dp),
                        )
                        item.progress.nextEpisode?.isFinale(item.progress.isLatestAired) == true -> FinaleChip(
                            contentTextStyle = TraktTheme.typography.meta.copy(
                                fontSize = 10.sp,
                            ),
                            modifier = Modifier
                                .shadow(
                                    2.dp,
                                    androidx.compose.foundation.shape.RoundedCornerShape(100),
                                )
                                .height(20.dp),
                        )
                    }

                    EpisodeProgressBar(
                        startText = rememberDurationFormat(runtime),
                        endText = stringResource(
                            R.string.tag_text_remaining_episodes,
                            remainingEpisodes,
                        ),
                        progress = remainingPercent,
                        modifier = Modifier
                            .shadow(
                                2.dp,
                                androidx.compose.foundation.shape.RoundedCornerShape(100),
                            ),
                    )
                }
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
                        .onClick(onClick = onShowClick)
                        .weight(1F, fill = false),
                ) {
                    Text(
                        text = item.show.title,
                        style = TraktTheme.typography.cardTitle,
                        color = TraktTheme.colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    Text(
                        text = item.progress.nextEpisode?.seasonEpisodeString() ?: "N/A",
                        style = TraktTheme.typography.cardSubtitle,
                        color = TraktTheme.colors.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                val checkSize = 20.dp
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .padding(start = 12.dp, end = 4.dp)
                        .size(checkSize),
                ) {
                    if (item.loading) {
                        FilmProgressIndicator(size = checkSize - 3.dp)
                    } else {
                        Icon(
                            painter = painterResource(R.drawable.ic_check),
                            contentDescription = null,
                            tint = TraktTheme.colors.accent,
                            modifier = Modifier
                                .size(checkSize)
                                .onClickCombined(
                                    onClick = onCheckClick,
                                    onLongClick = onCheckLongClick,
                                ),
                        )
                    }
                }
            }
        },
        modifier = modifier,
    )
}
