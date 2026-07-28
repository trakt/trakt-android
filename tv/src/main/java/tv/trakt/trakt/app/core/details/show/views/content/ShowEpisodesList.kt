package tv.trakt.trakt.app.core.details.show.views.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.app.common.ui.PositionFocusLazyRow
import tv.trakt.trakt.app.common.ui.chips.InfoChip
import tv.trakt.trakt.app.common.ui.mediacards.EpisodeSkeletonCard
import tv.trakt.trakt.app.common.ui.mediacards.HorizontalMediaCard
import tv.trakt.trakt.app.core.details.show.models.ShowSeasons.EpisodeItem
import tv.trakt.trakt.app.helpers.extensions.emptyFocusListItems
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.common.helpers.extensions.relativeDateTimeString
import tv.trakt.trakt.common.helpers.extensions.rememberDurationFormat
import tv.trakt.trakt.common.helpers.extensions.toLocal
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.resources.R

@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun ShowEpisodesList(
    isLoading: Boolean,
    show: Show?,
    episodes: () -> ImmutableList<EpisodeItem>,
    onFocused: () -> Unit,
    onEpisodeClick: (episode: Episode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val firstItem = remember { FocusRequester() }

    Column(
        horizontalAlignment = Alignment.Start,
        modifier = modifier
            .focusProperties {
                canFocus = isLoading
            },
    ) {
        if (isLoading) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(TraktTheme.spacing.mainRowSpace),
                contentPadding = PaddingValues(
                    start = TraktTheme.spacing.mainContentStartSpace,
                    end = TraktTheme.spacing.mainContentEndSpace,
                ),
            ) {
                items(count = 10) {
                    EpisodeSkeletonCard()
                }
            }
        } else {
            PositionFocusLazyRow(
                modifier = Modifier.focusRestorer(firstItem),
                contentPadding = PaddingValues(
                    start = TraktTheme.spacing.mainContentStartSpace,
                    end = TraktTheme.spacing.mainContentEndSpace,
                ),
            ) {
                itemsIndexed(
                    items = episodes(),
                    key = { _, item -> item.episode.ids.trakt.value },
                ) { index, (episode, watched) ->
                    HorizontalMediaCard(
                        title = "",
                        watched = watched,
                        containerImageUrl = when (episode.rememberReleased()) {
                            true -> episode.images?.getScreenshotUrl() ?: show?.images?.getFanartUrl()
                            false -> show?.images?.getFanartUrl()
                        },
                        onClick = { onEpisodeClick(episode) },
                        cardContent = {
                            if (!episode.rememberReleased()) {
                                InfoChip(
                                    text = episode.releasedAt?.toLocal()?.relativeDateTimeString() ?: "",
                                    iconPainter = painterResource(R.drawable.ic_calendar_upcoming),
                                    containerColor = TraktTheme.colors.chipContainerOnContent,
                                )
                            } else {
                                val runtime = episode.runtime?.inWholeMinutes
                                if (runtime != null) {
                                    InfoChip(
                                        text = rememberDurationFormat(runtime),
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
                        modifier = Modifier
                            .then(
                                if (index == 0) Modifier.focusRequester(firstItem) else Modifier,
                            )
                            .onFocusChanged {
                                if (it.isFocused) {
                                    onFocused()
                                }
                            },
                    )
                }

                emptyFocusListItems()
            }
        }
    }
}
