@file:OptIn(ExperimentalFoundationApi::class)
@file:Suppress("FunctionName")

package tv.trakt.trakt.core.summary.shows.features.seasons.all.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.extensions.rememberDurationFormat
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.toTraktId
import tv.trakt.trakt.core.summary.shows.features.seasons.all.AllShowSeasonsState
import tv.trakt.trakt.core.summary.shows.features.seasons.model.EpisodeItem
import tv.trakt.trakt.core.summary.shows.features.seasons.model.ShowSeasons
import tv.trakt.trakt.core.summary.shows.features.seasons.ui.EpisodeListItem
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.mediacards.skeletons.PanelHorizontalMediaSkeletonCard
import tv.trakt.trakt.ui.theme.TraktTheme

internal fun LazyListScope.SeasonEpisodesSection(
    state: AllShowSeasonsState,
    contentPadding: PaddingValues,
    onEpisodeClick: ((EpisodeItem) -> Unit)?,
    onCheckEpisodeClick: ((EpisodeItem) -> Unit)?,
    onCheckEpisodeLongClick: ((EpisodeItem) -> Unit)?,
    onMoreClick: ((EpisodeItem) -> Unit)?,
) {
    when {
        state.loading.isLoading || state.items.isSeasonLoading -> {
            item(
                key = "season_progress_summary_load",
            ) {
                SeasonProgressSummarySkeleton(
                    modifier = Modifier
                        .padding(contentPadding)
                        .padding(bottom = 23.dp),
                )
            }
            items(count = 10) {
                PanelHorizontalMediaSkeletonCard(
                    modifier = Modifier
                        .padding(contentPadding)
                        .padding(bottom = 12.dp),
                )
            }
        }
        else -> {
            item(
                key = "season_progress_summary",
            ) {
                val watchedEpisodes = remember(state.items.selectedSeasonEpisodes) {
                    state.items.selectedSeasonEpisodes.count { it.isWatched }
                }
                val leftEpisodes = remember(state.items.selectedSeasonEpisodes) {
                    state.items.selectedSeasonEpisodes.count { !it.isWatched }
                }
                val leftRuntime = remember(state.items.selectedSeasonEpisodes) {
                    state.items.selectedSeasonEpisodes
                        .filter { !it.isWatched }
                        .sumOf { it.episode.runtime?.inWholeMinutes ?: 0 }
                }
                val progressPercent = remember(state.items.selectedSeasonEpisodes) {
                    if (state.items.selectedSeasonEpisodes.isEmpty()) {
                        0F
                    } else {
                        watchedEpisodes.toFloat() / state.items.selectedSeasonEpisodes.size.toFloat()
                    }
                }
                SeasonProgressSummary(
                    season = state.items.selectedSeason?.number ?: 0,
                    startText = stringResource(R.string.text_stats_episodes_count, watchedEpisodes),
                    endText = when {
                        leftEpisodes == 0 -> {
                            ""
                        }
                        else -> {
                            "${
                                stringResource(
                                    R.string.tag_text_remaining_episodes,
                                    leftEpisodes,
                                )
                            }  •  ${rememberDurationFormat(leftRuntime)}"
                        }
                    },
                    progressPercent = progressPercent,
                    modifier = Modifier
                        .padding(contentPadding)
                        .padding(bottom = 23.dp),
                )
            }

            items(
                items = state.items.selectedSeasonEpisodes,
                key = { it.episode.ids.trakt.value },
            ) { item ->
                EpisodeListItem(
                    show = state.show!!,
                    episode = item,
                    onClick = onEpisodeClick,
                    onCheckClick = onCheckEpisodeClick,
                    onCheckLongClick = onCheckEpisodeLongClick,
                    onMoreClick = onMoreClick,
                    modifier = Modifier
                        .padding(contentPadding)
                        .padding(bottom = 12.dp)
                        .animateItem(fadeInSpec = null, fadeOutSpec = null),
                )
            }
        }
    }
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun PreviewSeasonEpisodesSection() {
    TraktTheme {
        val episodes = (1..6).map { n ->
            EpisodeItem(
                episode = PreviewData.episode1.copy(
                    ids = PreviewData.episode1.ids.copy(trakt = n.toTraktId()),
                    number = n,
                    title = "Episode $n",
                ),
                isWatched = n < 4,
                isCheckable = true,
            )
        }.toImmutableList()

        val contentPadding = PaddingValues(
            horizontal = TraktTheme.spacing.mainPageHorizontalSpace,
        )
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(TraktTheme.colors.backgroundPrimary),
        ) {
            SeasonEpisodesSection(
                state = AllShowSeasonsState(
                    show = PreviewData.show1,
                    loading = Done,
                    items = ShowSeasons(
                        selectedSeason = PreviewData.season1.copy(number = 1),
                        selectedSeasonEpisodes = episodes,
                    ),
                ),
                contentPadding = contentPadding,
                onEpisodeClick = null,
                onCheckEpisodeClick = null,
                onCheckEpisodeLongClick = null,
                onMoreClick = null,
            )
        }
    }
}
