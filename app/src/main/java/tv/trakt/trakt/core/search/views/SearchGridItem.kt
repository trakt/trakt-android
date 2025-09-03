package tv.trakt.trakt.core.search.views

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.core.search.model.SearchItem
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.InfoChip
import tv.trakt.trakt.ui.components.mediacards.VerticalMediaCard

@Composable
internal fun SearchGridItem(
    item: SearchItem,
    modifier: Modifier = Modifier,
    onShowClick: (Show) -> Unit = {},
    onMovieClick: (Movie) -> Unit = {},
) {
    when (item) {
        is SearchItem.Show -> {
            VerticalMediaCard(
                title = item.show.title,
                imageUrl = item.show.images?.getPosterUrl(),
                chipContent = {
                    Row(
                        horizontalArrangement = spacedBy(5.dp),
                    ) {
                        InfoChip(
                            text = item.show.released?.year?.toString()
                                ?: stringResource(R.string.translated_value_type_show),
                            iconPainter = painterResource(R.drawable.ic_shows_off),
                            iconPadding = 2.dp,
                        )
                    }
                },
                onClick = { onShowClick(item.show) },
                modifier = modifier,
            )
        }

        is SearchItem.Movie -> {
            VerticalMediaCard(
                title = item.movie.title,
                imageUrl = item.movie.images?.getPosterUrl(),
                chipContent = {
                    Row(
                        horizontalArrangement = spacedBy(5.dp),
                    ) {
                        InfoChip(
                            text = item.movie.released?.year?.toString()
                                ?: stringResource(R.string.translated_value_type_movie),
                            iconPainter = painterResource(R.drawable.ic_movies_off),
                            iconPadding = 1.dp,
                        )
                    }
                },
                onClick = { onMovieClick(item.movie) },
                modifier = modifier,
            )
        }
    }
}
