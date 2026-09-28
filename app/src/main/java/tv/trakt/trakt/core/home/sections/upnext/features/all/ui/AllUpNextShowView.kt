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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tv.trakt.trakt.common.core.home.model.Progress
import tv.trakt.trakt.common.core.home.model.UpNextShow
import tv.trakt.trakt.common.helpers.extensions.nowUtc
import tv.trakt.trakt.common.helpers.extensions.onClickCombined
import tv.trakt.trakt.common.helpers.extensions.rememberDurationFormat
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.EpisodeType
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.common.model.rememberEpisodeStatus
import tv.trakt.trakt.common.ui.composables.FilmProgressIndicator
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.EpisodeProgressBar
import tv.trakt.trakt.ui.components.chips.EpisodeStatusChip
import tv.trakt.trakt.ui.components.mediacards.PanelMediaCard
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun AllUpNextShowView(
    item: UpNextShow,
    enabled: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onCheckClick: () -> Unit,
    onCheckLongClick: () -> Unit,
    onShowClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val status = item.progress.nextEpisode?.rememberEpisodeStatus(item.progress.isLatestAired)

    PanelMediaCard(
        enabled = enabled,
        title = item.show.title,
        titleOriginal = when {
            status != null -> null
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

                val startString = remember(runtime) {
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
                    status?.let {
                        EpisodeStatusChip(
                            status = it,
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
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.size(20.dp),
                            ) {
                                FilmProgressIndicator(size = 16.dp)
                            }
                        } else {
                            Icon(
                                painter = painterResource(R.drawable.ic_check_2),
                                contentDescription = null,
                                tint = TraktTheme.colors.accent,
                                modifier = Modifier
                                    .size(20.dp)
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

@Preview
@Composable
private fun Preview() {
    TraktTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            listOf(
                PreviewData.episode1 to false,
                PreviewData.newPremiereEpisode to false,
                PreviewData.newFinaleEpisode to false,
                PreviewData.premiereEpisode to false,
                PreviewData.finaleEpisode to false,
                PreviewData.episode1.copy(type = EpisodeType.MID_SEASON_PREMIERE) to true,
            ).forEach { (episode, isLatestAired) ->
                AllUpNextShowView(
                    item = UpNextShow(
                        progress = Progress(
                            lastWatchedAt = nowUtc(),
                            aired = 12,
                            completed = 4,
                            stats = null,
                            nextEpisode = episode,
                            lastEpisode = null,
                            isLatestAired = isLatestAired,
                        ),
                        show = PreviewData.show1,
                    ),
                    enabled = true,
                    onClick = {},
                    onLongClick = {},
                    onCheckClick = {},
                    onCheckLongClick = {},
                    onShowClick = {},
                )
            }
        }
    }
}
