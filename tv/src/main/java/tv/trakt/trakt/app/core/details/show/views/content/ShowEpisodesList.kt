package tv.trakt.trakt.app.core.details.show.views.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.app.common.ui.InfoChip
import tv.trakt.trakt.app.common.ui.PositionFocusLazyRow
import tv.trakt.trakt.app.common.ui.mediacards.EpisodeSkeletonCard
import tv.trakt.trakt.app.common.ui.mediacards.HorizontalMediaCard
import tv.trakt.trakt.app.core.episodes.model.Episode
import tv.trakt.trakt.app.helpers.extensions.durationFormat
import tv.trakt.trakt.app.helpers.extensions.emptyFocusListItems
import tv.trakt.trakt.app.ui.theme.TraktTheme

@Composable
internal fun ShowEpisodesList(
    isLoading: Boolean,
    episodes: () -> ImmutableList<Episode>,
    onFocused: () -> Unit,
    onEpisodeClick: (episode: Episode) -> Unit,
    modifier: Modifier = Modifier,
) {
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
                contentPadding = PaddingValues(
                    start = TraktTheme.spacing.mainContentStartSpace,
                    end = TraktTheme.spacing.mainContentEndSpace,
                ),
            ) {
                items(
                    items = episodes(),
                    key = { item -> item.ids.trakt.value },
                ) { episode ->
                    HorizontalMediaCard(
                        title = "",
                        containerImageUrl = episode.images?.getScreenshotUrl(),
                        onClick = { onEpisodeClick(episode) },
                        cardContent = {
                            val runtime = episode.runtime?.inWholeMinutes
                            if (runtime != null) {
                                InfoChip(
                                    text = runtime.durationFormat(),
                                    modifier = Modifier.padding(end = 8.dp),
                                )
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
                                    text = episode.seasonEpisodeString,
                                    style = TraktTheme.typography.cardSubtitle,
                                    color = TraktTheme.colors.textSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        },
                        modifier = Modifier
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
