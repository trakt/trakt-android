@file:OptIn(ExperimentalFoundationApi::class)

package tv.trakt.trakt.core.summary.shows.features.seasons.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.layout.LazyLayoutCacheWindow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.extensions.durationFormat
import tv.trakt.trakt.common.helpers.extensions.nowUtc
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.extensions.relativeDateTimeString
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.ui.composables.FilmProgressIndicator
import tv.trakt.trakt.common.ui.theme.colors.Red500
import tv.trakt.trakt.core.summary.shows.features.seasons.model.EpisodeItem
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.InfoChip
import tv.trakt.trakt.ui.components.mediacards.HorizontalMediaCard
import tv.trakt.trakt.ui.components.mediacards.skeletons.EpisodeSkeletonCard
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun ShowEpisodesList(
    isLoading: Boolean,
    show: Show?,
    season: Int?,
    episodes: ImmutableList<EpisodeItem>,
    onEpisodeClick: (episode: EpisodeItem) -> Unit,
    onCheckClick: (episode: EpisodeItem) -> Unit,
    onRemoveClick: (episode: EpisodeItem) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState(
        cacheWindow = LazyLayoutCacheWindow(
            aheadFraction = 1F,
            behindFraction = 1F,
        ),
    )

    var currentSeason by remember { mutableIntStateOf(season ?: 0) }
    LaunchedEffect(season) {
        if (currentSeason != season) {
            listState.animateScrollToItem(0)
            currentSeason = season ?: 0
        }
    }

    Column(
        horizontalAlignment = Alignment.Start,
        modifier = modifier,
    ) {
        if (isLoading) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(TraktTheme.spacing.mainRowSpace),
                contentPadding = contentPadding,
            ) {
                items(count = 3) {
                    EpisodeSkeletonCard()
                }
            }
        } else {
            LazyRow(
                state = listState,
                horizontalArrangement = Arrangement.spacedBy(TraktTheme.spacing.mainRowSpace),
                contentPadding = contentPadding,
                modifier = Modifier.fillMaxWidth(),
            ) {
                items(
                    items = episodes,
                    key = { item -> item.episode.ids.trakt.value },
                ) { item ->
                    val isReleased = remember(item.episode.firstAired) {
                        val firstAired = item.episode.firstAired
                        firstAired != null && firstAired.isBefore(nowUtc())
                    }

                    HorizontalMediaCard(
                        title = "",
                        containerImageUrl = item.episode.images?.getScreenshotUrl()
                            ?: show?.images?.getFanartUrl(),
                        onClick = { onEpisodeClick(item) },
                        cardContent = {
                            if (!isReleased) {
                                InfoChip(
                                    text = item.episode.firstAired?.relativeDateTimeString() ?: "",
                                    iconPainter = painterResource(R.drawable.ic_calendar_upcoming),
                                    containerColor = TraktTheme.colors.chipContainerOnContent,
                                )
                            } else {
                                val runtime = item.episode.runtime?.inWholeMinutes
                                if (runtime != null) {
                                    InfoChip(
                                        text = runtime.durationFormat(),
                                        containerColor = TraktTheme.colors.chipContainerOnContent,
                                    )
                                }
                            }
                        },
                        footerContent = {
                            Row(
                                verticalAlignment = CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(1.dp),
                                    modifier = Modifier.weight(1F, fill = false),
                                ) {
                                    Text(
                                        text = item.episode.title,
                                        style = TraktTheme.typography.cardTitle,
                                        color = TraktTheme.colors.textPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )

                                    Text(
                                        text = item.episode.seasonEpisode.toDisplayString(),
                                        style = TraktTheme.typography.cardSubtitle,
                                        color = TraktTheme.colors.textSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }

                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .padding(start = 12.dp, end = 4.dp)
                                        .size(23.dp),
                                ) {
                                    when {
                                        item.isLoading -> {
                                            FilmProgressIndicator(
                                                size = 19.dp,
                                                modifier = Modifier
                                                    .graphicsLayer {
                                                        translationX = 2.dp.toPx()
                                                    },
                                            )
                                        }
                                        item.isWatched -> {
                                            Icon(
                                                painter = painterResource(R.drawable.ic_close),
                                                contentDescription = null,
                                                tint = Red500,
                                                modifier = Modifier
                                                    .size(23.dp)
                                                    .graphicsLayer {
                                                        translationX = 3.dp.toPx()
                                                    }
                                                    .onClick {
                                                        onRemoveClick(item)
                                                    },
                                            )
                                        }
                                        isReleased && item.isCheckable -> {
                                            Icon(
                                                painter = painterResource(R.drawable.ic_check_round),
                                                contentDescription = null,
                                                tint = TraktTheme.colors.accent,
                                                modifier = Modifier
                                                    .size(19.dp)
                                                    .onClick {
                                                        onCheckClick(item)
                                                    },
                                            )
                                        }
                                    }
                                }
                            }
                        },
                        modifier = Modifier.animateItem(
                            fadeInSpec = null,
                            fadeOutSpec = null,
                        ),
                    )
                }
            }
        }
    }
}
