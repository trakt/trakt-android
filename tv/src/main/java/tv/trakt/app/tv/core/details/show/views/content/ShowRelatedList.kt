package tv.trakt.app.tv.core.details.show.views.content

import InfoChip
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
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.app.tv.R
import tv.trakt.app.tv.common.ui.PositionFocusLazyRow
import tv.trakt.app.tv.common.ui.mediacards.HorizontalMediaCard
import tv.trakt.app.tv.core.shows.model.Show
import tv.trakt.app.tv.helpers.extensions.emptyFocusListItems
import tv.trakt.app.tv.ui.theme.TraktTheme

@Composable
internal fun ShowRelatedList(
    header: String,
    shows: () -> ImmutableList<Show>,
    onFocused: () -> Unit,
    onClick: (Show) -> Unit,
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
                HorizontalMediaCard(
                    title = show.title,
                    containerImageUrl = show.images?.getFanartUrl(),
                    contentImageUrl = show.images?.getLogoUrl(),
                    paletteColor = show.colors?.colors?.second,
                    onClick = { onClick(show) },
                    footerContent = {
                        val episodes = show.airedEpisodes
                        if (episodes > 0) {
                            InfoChip(
                                text = stringResource(R.string.episodes_number, show.airedEpisodes),
                                modifier = Modifier.padding(end = 8.dp),
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
