@file:OptIn(ExperimentalFoundationApi::class)

package tv.trakt.trakt.core.summary.shows.features.seasons.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.layout.LazyLayoutCacheWindow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.extensions.durationFormat
import tv.trakt.trakt.common.helpers.extensions.nowUtc
import tv.trakt.trakt.common.helpers.extensions.relativeDateTimeString
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.InfoChip
import tv.trakt.trakt.ui.components.mediacards.HorizontalMediaCard
import tv.trakt.trakt.ui.components.mediacards.skeletons.EpisodeSkeletonCard
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun ShowEpisodesList(
    isLoading: Boolean,
    show: Show?,
    episodes: ImmutableList<Episode>,
    onEpisodeClick: (episode: Episode) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val listHash = rememberSaveable { mutableIntStateOf(episodes.hashCode()) }

    val listState = rememberLazyListState(
        cacheWindow = LazyLayoutCacheWindow(
            aheadFraction = 1F,
            behindFraction = 1F,
        ),
    )

    LaunchedEffect(episodes) {
        val hash = episodes.hashCode()
        if (listHash.intValue != hash) {
            listHash.intValue = hash
            listState.animateScrollToItem(0)
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
                    key = { item -> item.ids.trakt.value },
                ) { episode ->
                    HorizontalMediaCard(
                        title = "",
                        containerImageUrl = episode.images?.getScreenshotUrl()
                            ?: show?.images?.getFanartUrl(),
                        onClick = { onEpisodeClick(episode) },
                        cardContent = {
                            val isReleased = remember(episode.firstAired) {
                                val firstAired = episode.firstAired
                                firstAired != null && !firstAired.isBefore(nowUtc())
                            }
                            if (isReleased) {
                                InfoChip(
                                    text = episode.firstAired?.relativeDateTimeString() ?: "",
                                    iconPainter = painterResource(R.drawable.ic_calendar_upcoming),
                                    containerColor = TraktTheme.colors.chipContainerOnContent,
                                )
                            } else {
                                val runtime = episode.runtime?.inWholeMinutes
                                if (runtime != null) {
                                    InfoChip(
                                        text = runtime.durationFormat(),
                                        containerColor = TraktTheme.colors.chipContainerOnContent,
                                    )
                                }
                            }
                        },
                        footerContent = {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(1.dp),
                            ) {
                                Text(
                                    text = episode.title,
                                    style = TraktTheme.typography.cardTitle,
                                    color = TraktTheme.colors.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )

                                Text(
                                    text = episode.seasonEpisode.toDisplayString(),
                                    style = TraktTheme.typography.cardSubtitle,
                                    color = TraktTheme.colors.textSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
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
