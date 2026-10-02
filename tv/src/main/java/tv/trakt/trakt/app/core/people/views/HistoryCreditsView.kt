package tv.trakt.trakt.app.core.people.views

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import tv.trakt.trakt.app.common.ui.PositionFocusLazyRow
import tv.trakt.trakt.app.common.ui.mediacards.VerticalMediaCard
import tv.trakt.trakt.app.core.people.model.PersonHistoryItem
import tv.trakt.trakt.app.helpers.extensions.emptyFocusListItems
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.common.helpers.extensions.rememberDurationFormat
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.resources.R

@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun HistoryCreditsView(
    header: String,
    items: ImmutableList<PersonHistoryItem>,
    onFocused: () -> Unit,
    onShowClick: (Show) -> Unit,
    onMovieClick: (Movie) -> Unit,
    modifier: Modifier = Modifier,
) {
    val firstItem = remember { FocusRequester() }

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
            modifier = Modifier.focusRestorer(firstItem),
            contentPadding = PaddingValues(
                start = TraktTheme.spacing.mainContentStartSpace,
                end = TraktTheme.spacing.mainContentEndSpace,
            ),
        ) {
            itemsIndexed(
                items = items,
                key = { _, item -> item.key },
            ) { index, item ->
                val cardModifier = Modifier
                    .then(
                        if (index == 0) Modifier.focusRequester(firstItem) else Modifier,
                    )
                    .onFocusChanged {
                        if (it.hasFocus) onFocused()
                    }

                when (item) {
                    is PersonHistoryItem.ShowItem -> ShowItemCard(
                        show = item.show,
                        onClick = { onShowClick(item.show) },
                        modifier = cardModifier,
                    )

                    is PersonHistoryItem.MovieItem -> MovieItemCard(
                        movie = item.movie,
                        onClick = { onMovieClick(item.movie) },
                        modifier = cardModifier,
                    )
                }
            }

            emptyFocusListItems()
        }
    }
}

@Composable
private fun ShowItemCard(
    show: Show,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    VerticalMediaCard(
        title = show.title,
        imageUrl = show.images?.getPosterUrl(),
        watched = true,
        onClick = onClick,
        chipContent = {
            val episodes = show.airedEpisodes.takeIf { it > 0 }
                ?.let { stringResource(R.string.tag_text_number_of_episodes, it) }

            CardChip(
                iconRes = R.drawable.ic_shows_off,
                text = listOfNotNull(show.year?.toString(), episodes)
                    .joinToString(" • "),
            )
        },
        modifier = modifier,
    )
}

@Composable
private fun MovieItemCard(
    movie: Movie,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    VerticalMediaCard(
        title = movie.title,
        imageUrl = movie.images?.getPosterUrl(),
        watched = true,
        onClick = onClick,
        chipContent = {
            CardChip(
                iconRes = R.drawable.ic_movies_off,
                text = buildString {
                    append(movie.yearString)
                    movie.runtime?.inWholeMinutes?.let {
                        if (isNotEmpty()) append(" • ")
                        append(rememberDurationFormat(it))
                    }
                },
            )
        },
        modifier = modifier,
    )
}

@Composable
private fun CardChip(
    @DrawableRes iconRes: Int,
    text: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = TraktTheme.colors.textPrimary,
            modifier = Modifier.size(12.dp),
        )

        Text(
            text = text,
            style = TraktTheme.typography.cardTitle,
            color = TraktTheme.colors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Preview(
    device = "id:tv_4k",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview() {
    TraktTheme {
        HistoryCreditsView(
            header = stringResource(R.string.list_title_from_my_history),
            items = persistentListOf(
                PersonHistoryItem.ShowItem(PreviewData.show1),
                PersonHistoryItem.MovieItem(PreviewData.movie1),
                PersonHistoryItem.MovieItem(PreviewData.movie2),
            ),
            onFocused = {},
            onShowClick = {},
            onMovieClick = {},
        )
    }
}
