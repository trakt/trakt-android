package tv.trakt.app.tv.core.details.episode.views.content

import InfoChip
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.app.tv.common.ui.PositionFocusLazyRow
import tv.trakt.app.tv.common.ui.mediacards.HorizontalMediaCard
import tv.trakt.app.tv.core.episodes.model.Episode
import tv.trakt.app.tv.helpers.extensions.durationFormat
import tv.trakt.app.tv.helpers.extensions.emptyFocusListItems
import tv.trakt.app.tv.ui.theme.TraktTheme

@Composable
internal fun EpisodeSeasonEpisodesList(
    header1: String,
    header2: String,
    episodes: () -> ImmutableList<Episode>,
    onFocused: () -> Unit,
    onClicked: (Episode) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.padding(
                start = TraktTheme.spacing.mainContentStartSpace,
            ),
        ) {
            Text(
                text = header1,
                color = TraktTheme.colors.textSecondary,
                style = TraktTheme.typography.heading4,
            )
            Text(
                text = header2,
                color = TraktTheme.colors.textPrimary,
                style = TraktTheme.typography.heading4,
            )
        }

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
                    onClick = { onClicked(episode) },
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
