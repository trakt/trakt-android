package tv.trakt.trakt.app.core.details.episode.views.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.app.common.ui.InfoChip
import tv.trakt.trakt.app.common.ui.PositionFocusLazyRow
import tv.trakt.trakt.app.common.ui.mediacards.HorizontalMediaCard
import tv.trakt.trakt.app.core.episodes.model.Episode
import tv.trakt.trakt.app.helpers.extensions.emptyFocusListItems
import tv.trakt.trakt.app.helpers.extensions.nowUtc
import tv.trakt.trakt.app.helpers.extensions.relativeDateTimeString
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.common.helpers.extensions.durationFormat
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.resources.R

@Composable
internal fun EpisodeSeasonEpisodesList(
    header1: String,
    header2: String,
    show: Show?,
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
                    containerImageUrl = episode.images?.getScreenshotUrl()
                        ?: show?.images?.getFanartUrl(),
                    onClick = { onClicked(episode) },
                    cardContent = {
                        val isReleased = remember(episode.firstAired) {
                            episode.firstAired != null && !episode.firstAired.isBefore(nowUtc())
                        }
                        if (isReleased) {
                            InfoChip(
                                text = episode.firstAired?.relativeDateTimeString() ?: "",
                                iconPainter = painterResource(R.drawable.ic_calendar_upcoming),
                                containerColor = TraktTheme.colors.chipContainer.copy(alpha = 0.7F),
                            )
                        } else {
                            val runtime = episode.runtime?.inWholeMinutes
                            if (runtime != null) {
                                InfoChip(
                                    text = runtime.durationFormat(),
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
                                text = episode.seasonEpisode.toString(),
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
