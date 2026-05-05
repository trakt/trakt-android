package tv.trakt.trakt.core.home.sections.upnext.features.all.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tv.trakt.trakt.common.helpers.extensions.onClickCombined
import tv.trakt.trakt.common.helpers.extensions.rememberDurationFormat
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.common.ui.composables.FilmProgressIndicator
import tv.trakt.trakt.core.home.sections.upnext.model.UpNextShow
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.EpisodeProgressBar
import tv.trakt.trakt.ui.components.chips.FinaleChip
import tv.trakt.trakt.ui.components.chips.PremiereChip
import tv.trakt.trakt.ui.components.mediacards.PanelMediaCard
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun AllUpNextShowView(
    item: UpNextShow,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onCheckClick: () -> Unit,
    onCheckLongClick: () -> Unit,
    onShowClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isPremiere = item.progress.nextEpisode?.isPremiere() == true
    val isFinale = item.progress.nextEpisode?.isFinale() == true

    PanelMediaCard(
        title = item.show.title,
        titleOriginal = when {
            isPremiere || isFinale -> null
            else -> item.show.titleOriginal
        },
        subtitle = item.progress.nextEpisode?.seasonEpisodeString() ?: "N/A",
        contentImageUrl = item.show.images?.getPosterUrl(),
        containerImageUrl = item.progress.nextEpisode?.images?.getScreenshotUrl(Images.Size.THUMB)
            ?: item.show.images?.getFanartUrl(Images.Size.THUMB),
        onClick = onClick,
        onLongClick = onLongClick,
        onImageClick = onShowClick,
        footerContent = {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                val runtime = rememberDurationFormat(
                    item.progress.nextEpisode?.runtime?.inWholeMinutes
                        ?: item.show.runtime?.inWholeMinutes,
                )

                val startString = remember {
                    buildString {
                        if (runtime != "N/A") {
                            append(runtime)
                        }
                    }
                }

                val remainingEpisodesString = stringResource(
                    R.string.tag_text_remaining_episodes,
                    item.progress.remainingEpisodes,
                )
                val remainingMinutesString = rememberDurationFormat(item.progress.remainingMinutes)

                val endString = remember {
                    val separator = "  •  "
                    buildString {
                        val remainingEpisodes = item.progress.remainingEpisodes
                        if (remainingEpisodes > 0) {
                            append(remainingEpisodesString)
                        }

                        append(separator)

                        val remainingTime = item.progress.remainingMinutes
                        if (remainingTime != null) {
                            append(remainingMinutesString)
                        }
                    }
                }

                val remainingPercent = remember(
                    item.progress.completed,
                    item.progress.aired,
                ) {
                    item.progress.remainingPercent
                }

                Column(
                    verticalArrangement = Arrangement.Absolute.spacedBy(4.dp),
                ) {
                    when {
                        isPremiere -> PremiereChip(
                            contentTextStyle = TraktTheme.typography.meta.copy(
                                fontSize = 10.sp,
                            ),
                            containerColor = TraktTheme.colors.chipContainer,
                            modifier = Modifier
                                .height(20.dp),
                        )
                        isFinale -> FinaleChip(
                            contentTextStyle = TraktTheme.typography.meta.copy(
                                fontSize = 10.sp,
                            ),
                            containerColor = TraktTheme.colors.chipContainer,
                            modifier = Modifier
                                .height(20.dp),
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        EpisodeProgressBar(
                            startText = startString,
                            endText = endString,
                            progress = remainingPercent,
                            containerColor = TraktTheme.colors.chipContainer,
                            modifier = Modifier.weight(1F, fill = false),
                        )

                        if (item.loading) {
                            Box(modifier = Modifier.size(18.dp)) {
                                FilmProgressIndicator(size = 16.dp)
                            }
                        } else {
                            Icon(
                                painter = painterResource(R.drawable.ic_check),
                                contentDescription = null,
                                tint = TraktTheme.colors.accent,
                                modifier = Modifier
                                    .size(18.dp)
                                    .onClickCombined(
                                        onClick = onCheckClick,
                                        onLongClick = onCheckLongClick,
                                    ),
                            )
                        }
                    }
                }
            }
        },
        modifier = modifier
            .padding(bottom = TraktTheme.spacing.mainListVerticalSpace),
    )
}
