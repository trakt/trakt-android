package tv.trakt.trakt.app.core.details.episode.views.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.app.common.ui.PositionFocusLazyRow
import tv.trakt.trakt.app.common.ui.mediacards.VerticalMediaCard
import tv.trakt.trakt.app.helpers.extensions.emptyFocusListItems
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.resources.R

@Composable
internal fun EpisodeRelatedList(
    header: String,
    shows: () -> ImmutableList<Show>,
    onFocused: () -> Unit,
    onClicked: (Show) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier,
    ) {
        Text(
            text = header,
            color = TraktTheme.colors.textPrimary,
            style = TraktTheme.typography.heading4,
            modifier = Modifier.padding(
                start = TraktTheme.spacing.mainContentStartSpace,
            ),
        )

        PositionFocusLazyRow(
            contentPadding = PaddingValues(
                start = TraktTheme.spacing.mainContentStartSpace,
                end = TraktTheme.spacing.mainContentEndSpace,
            ),
        ) {
            items(
                items = shows(),
                key = { it.ids.trakt.value },
            ) { show ->
                VerticalMediaCard(
                    title = show.title,
                    imageUrl = show.images?.getPosterUrl(),
                    onClick = { onClicked(show) },
                    chipContent = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(1.dp),
                        ) {
                            val episodes = show.airedEpisodes.takeIf { it > 0 }
                                ?.let { stringResource(R.string.tag_text_number_of_episodes, it) }
                            val text = listOfNotNull(show.year?.toString(), episodes)
                                .joinToString("  •  ")
                            if (text.isNotEmpty()) {
                                Text(
                                    text = text,
                                    style = TraktTheme.typography.cardTitle,
                                    color = TraktTheme.colors.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
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
