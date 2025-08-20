package tv.trakt.trakt.app.core.details.show.views.content

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
import tv.trakt.trakt.app.common.ui.PositionFocusLazyRow
import tv.trakt.trakt.app.common.ui.mediacards.VerticalMediaCard
import tv.trakt.trakt.app.core.details.show.models.ShowSeasons
import tv.trakt.trakt.app.core.episodes.model.Season
import tv.trakt.trakt.app.helpers.extensions.emptyFocusListItems
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.common.model.Images.Size.MEDIUM
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.resources.R

@Composable
internal fun ShowSeasonsList(
    header1: String,
    header2: String,
    show: Show?,
    seasons: () -> ShowSeasons,
    onFocused: () -> Unit,
    onSeasonClick: (Season) -> Unit,
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
                items = seasons().seasons,
                key = { item -> item.ids.trakt.value },
            ) { item ->
                val seasonPosterUrl = item.images?.getPosterUrl(MEDIUM)
                val showPosterUrl = show?.images?.getPosterUrl(MEDIUM)
                VerticalMediaCard(
                    title = "",
                    imageUrl = seasonPosterUrl ?: showPosterUrl,
                    onClick = { onSeasonClick(item) },
                    chipContent = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(1.dp),
                        ) {
                            val seasonTitle = when {
                                item.isSpecial -> stringResource(R.string.text_season_specials)
                                else -> stringResource(R.string.text_season_number, item.number)
                            }

                            Text(
                                text = seasonTitle,
                                style = TraktTheme.typography.cardTitle,
                                color = TraktTheme.colors.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )

                            item.episodeCount?.let {
                                Text(
                                    text = stringResource(R.string.tag_text_number_of_episodes, it),
                                    style = TraktTheme.typography.cardSubtitle,
                                    color = TraktTheme.colors.textSecondary,
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
