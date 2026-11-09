package tv.trakt.trakt.core.summary.shows.features.progress.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableSet
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.summary.shows.features.progress.model.SeasonProgress
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.buttons.GhostButton
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun SeasonProgressRow(
    season: SeasonProgress,
    expanded: Boolean,
    episodes: ImmutableList<Episode>?,
    upNextEpisodeId: TraktId?,
    busyEpisodes: ImmutableSet<TraktId>,
    modifier: Modifier = Modifier,
    onToggle: () -> Unit = {},
    onEpisodeClick: (Episode, watched: Boolean) -> Unit = { _, _ -> },
    onFinishClick: (List<Episode>) -> Unit = {},
) {
    val caretRotation by animateFloatAsState(
        targetValue = if (expanded) 90F else 0F,
        label = "caretRotation",
    )

    Column(
        verticalArrangement = spacedBy(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
    ) {
        Column(
            verticalArrangement = spacedBy(8.dp),
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .onClick(onClick = onToggle),
        ) {
            Row(
                verticalAlignment = CenterVertically,
                horizontalArrangement = spacedBy(8.dp),
            ) {
                Text(
                    text = when (season.number) {
                        0 -> stringResource(R.string.text_season_specials)
                        else -> stringResource(R.string.text_season_number, season.number)
                    },
                    style = TraktTheme.typography.cardTitle,
                    color = TraktTheme.colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1F),
                )

                Text(
                    text = when {
                        season.aired == 0 -> stringResource(R.string.text_watch_state_not_aired)
                        season.isComplete -> stringResource(R.string.text_watch_state_complete)
                        season.watched == 0 -> stringResource(R.string.text_season_not_started)
                        else -> stringResource(R.string.text_watch_state_count, season.watched, season.aired)
                    },
                    style = TraktTheme.typography.meta,
                    color = TraktTheme.colors.textSecondary,
                    maxLines = 1,
                )

                Icon(
                    painter = painterResource(R.drawable.ic_chevron_right),
                    tint = TraktTheme.colors.textSecondary,
                    contentDescription = null,
                    modifier = Modifier
                        .size(14.dp)
                        .rotate(caretRotation),
                )
            }

            WatchProgressLine(
                progress = season.progress,
                lit = expanded && !season.isComplete,
            )
        }

        AnimatedVisibility(visible = expanded) {
            SeasonEpisodes(
                season = season,
                episodes = episodes,
                upNextEpisodeId = upNextEpisodeId,
                busyEpisodes = busyEpisodes,
                onEpisodeClick = onEpisodeClick,
                onFinishClick = onFinishClick,
            )
        }
    }
}

@Composable
private fun SeasonEpisodes(
    season: SeasonProgress,
    episodes: ImmutableList<Episode>?,
    upNextEpisodeId: TraktId?,
    busyEpisodes: ImmutableSet<TraktId>,
    onEpisodeClick: (Episode, watched: Boolean) -> Unit,
    onFinishClick: (List<Episode>) -> Unit,
) {
    val remaining = remember(episodes, season.watchedEpisodeIds) {
        episodes
            ?.filter { it.isReleased && it.ids.trakt !in season.watchedEpisodeIds }
            .orEmpty()
    }

    Column(
        verticalArrangement = spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        FlowRow(
            horizontalArrangement = spacedBy(12.dp),
            verticalArrangement = spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (episodes == null) {
                repeat(season.total) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(TraktTheme.colors.skeletonContainer, CircleShape),
                    )
                }
            } else {
                episodes.forEach { episode ->
                    val watched = episode.ids.trakt in season.watchedEpisodeIds
                    EpisodeProgressChip(
                        number = episode.number,
                        state = when {
                            watched -> EpisodeWatchState.Watched
                            !episode.isReleased -> EpisodeWatchState.Upcoming
                            else -> EpisodeWatchState.Unwatched
                        },
                        upNext = episode.ids.trakt == upNextEpisodeId,
                        busy = episode.ids.trakt in busyEpisodes,
                        onClick = { onEpisodeClick(episode, watched) },
                    )
                }
            }
        }

        if (remaining.isNotEmpty()) {
            GhostButton(
                text = stringResource(R.string.button_text_finish_season),
                icon = painterResource(R.drawable.ic_check),
                iconSize = 16.dp,
                fillWidth = false,
                uppercase = false,
                enabled = remaining.none { it.ids.trakt in busyEpisodes },
                onClick = { onFinishClick(remaining) },
                modifier = Modifier.align(Alignment.End),
            )
        }
    }
}

@PreviewTraktLightDark
@Composable
private fun Preview() {
    val episodes = (1..8).map {
        PreviewData.episode1.copy(
            ids = PreviewData.episode1.ids.copy(trakt = TraktId(it)),
            number = it,
        )
    }.toImmutableList()

    TraktThemePreview {
        Column(
            verticalArrangement = spacedBy(8.dp),
            modifier = Modifier.padding(16.dp),
        ) {
            SeasonProgressRow(
                season = SeasonProgress(
                    number = 1,
                    watched = 3,
                    aired = 8,
                    total = 8,
                    watchedEpisodeIds = (1..3).map { TraktId(it) }.toImmutableSet(),
                ),
                expanded = true,
                episodes = episodes,
                upNextEpisodeId = TraktId(4),
                busyEpisodes = persistentSetOf(),
            )
            SeasonProgressRow(
                season = SeasonProgress(
                    number = 2,
                    watched = 0,
                    aired = 0,
                    total = 10,
                    watchedEpisodeIds = persistentSetOf(),
                ),
                expanded = false,
                episodes = persistentListOf(),
                upNextEpisodeId = null,
                busyEpisodes = persistentSetOf(),
            )
        }
    }
}
