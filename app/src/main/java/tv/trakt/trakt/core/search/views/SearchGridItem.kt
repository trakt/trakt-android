package tv.trakt.trakt.core.search.views

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.helpers.extensions.durationFormat
import tv.trakt.trakt.common.model.Images.Size.THUMB
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Person
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.core.search.model.SearchFilter
import tv.trakt.trakt.core.search.model.SearchItem
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.InfoChip
import tv.trakt.trakt.ui.components.mediacards.VerticalMediaCard
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun SearchGridItem(
    item: SearchItem,
    filter: SearchFilter,
    modifier: Modifier = Modifier,
    onShowClick: (Show) -> Unit = {},
    onMovieClick: (Movie) -> Unit = {},
    onPersonClick: (Person) -> Unit = {},
) {
    when (item) {
        is SearchItem.Show -> ShowGridItem(item, filter, onShowClick, modifier)
        is SearchItem.Movie -> MovieGridItem(item, filter, onMovieClick, modifier)
        is SearchItem.Person -> PersonGridItem(item, onPersonClick, modifier)
    }
}

@Composable
private fun ShowGridItem(
    item: SearchItem.Show,
    filter: SearchFilter,
    onShowClick: (Show) -> Unit,
    modifier: Modifier,
) {
    VerticalMediaCard(
        title = item.show.title,
        imageUrl = item.show.images?.getPosterUrl(),
        chipContent = {
            if (filter == SearchFilter.MEDIA) {
                InfoChip(
                    text = item.show.released?.year?.toString()
                        ?: stringResource(R.string.translated_value_type_show),
                    iconPainter = painterResource(R.drawable.ic_shows_off),
                    iconPadding = 2.dp,
                )
            } else {
                Row(
                    horizontalArrangement = spacedBy(5.dp),
                ) {
                    if ((item.show.year ?: 0) > 0) {
                        InfoChip(
                            text = item.show.year.toString(),
                        )
                    }
                    InfoChip(
                        text = stringResource(R.string.tag_text_number_of_episodes_short, item.show.airedEpisodes),
                    )
                }
            }
        },
        onClick = { onShowClick(item.show) },
        modifier = modifier,
    )
}

@Composable
private fun MovieGridItem(
    item: SearchItem.Movie,
    filter: SearchFilter,
    onMovieClick: (Movie) -> Unit,
    modifier: Modifier,
) {
    VerticalMediaCard(
        title = item.movie.title,
        imageUrl = item.movie.images?.getPosterUrl(),
        chipContent = {
            if (filter == SearchFilter.MEDIA) {
                InfoChip(
                    text = item.movie.released?.year?.toString()
                        ?: stringResource(R.string.translated_value_type_movie),
                    iconPainter = painterResource(R.drawable.ic_movies_off),
                    iconPadding = 1.dp,
                )
            } else {
                Row(
                    horizontalArrangement = spacedBy(5.dp),
                ) {
                    if ((item.movie.year ?: 0) > 0) {
                        InfoChip(
                            text = item.movie.year.toString(),
                        )
                    }
                    item.movie.runtime?.inWholeMinutes?.let {
                        val runtimeString = remember(item.movie.runtime) {
                            it.durationFormat()
                        }
                        InfoChip(
                            text = runtimeString,
                        )
                    }
                }
            }
        },
        onClick = { onMovieClick(item.movie) },
        modifier = modifier,
    )
}

@Composable
private fun PersonGridItem(
    item: SearchItem.Person,
    onPersonClick: (Person) -> Unit,
    modifier: Modifier,
) {
    VerticalMediaCard(
        title = item.person.name,
        imageUrl = item.person.images?.getHeadshotUrl(THUMB),
        chipContent = {
            Column(
                verticalArrangement = spacedBy(1.dp),
            ) {
                Text(
                    text = item.person.name,
                    style = TraktTheme.typography.cardTitle,
                    color = TraktTheme.colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                item.person.knownForDepartment?.let { string ->
                    Text(
                        text = string.replaceFirstChar {
                            it.uppercaseChar()
                        },
                        style = TraktTheme.typography.cardSubtitle,
                        color = TraktTheme.colors.textSecondary,
                    )
                }
            }
        },
        onClick = { onPersonClick(item.person) },
        modifier = modifier,
    )
}
