package tv.trakt.trakt.app.core.lists.views

import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.tv.material3.Text
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.app.R
import tv.trakt.trakt.app.common.ui.InfoChip
import tv.trakt.trakt.app.common.ui.PositionFocusLazyRow
import tv.trakt.trakt.app.common.ui.mediacards.HorizontalMediaCard
import tv.trakt.trakt.app.common.ui.mediacards.HorizontalViewAllCard
import tv.trakt.trakt.app.core.lists.ListsConfig.LISTS_SECTION_LIMIT
import tv.trakt.trakt.app.core.lists.ListsContentLoading
import tv.trakt.trakt.app.core.movies.model.Movie
import tv.trakt.trakt.app.helpers.extensions.durationFormat
import tv.trakt.trakt.app.helpers.extensions.emptyFocusListItems
import tv.trakt.trakt.app.ui.theme.TraktTheme

@Composable
internal fun ListsMoviesWatchlistView(
    items: ImmutableList<Movie>?,
    isLoading: Boolean,
    focusRequesters: Map<String, FocusRequester>,
    onFocused: (Movie?) -> Unit,
    onClick: (Movie) -> Unit,
    onViewAllClick: () -> Unit,
) {
    val contentPadding = PaddingValues(
        start = TraktTheme.spacing.mainContentStartSpace,
        end = TraktTheme.spacing.mainContentEndSpace,
    )

    Column(
        verticalArrangement = spacedBy(TraktTheme.spacing.mainRowHeaderSpace),
        modifier = Modifier
            .focusGroup()
            .focusRequester(focusRequesters.getValue("movies")),
    ) {
        Text(
            text = stringResource(R.string.header_movies_watchlist),
            color = TraktTheme.colors.textPrimary,
            style = TraktTheme.typography.heading5,
            modifier = Modifier.padding(contentPadding),
        )

        when {
            isLoading -> {
                ListsContentLoading(
                    contentPadding = contentPadding,
                )
            }

            items?.isEmpty() == true -> {
                Text(
                    text = stringResource(R.string.info_generic_empty_list),
                    color = TraktTheme.colors.textSecondary,
                    style = TraktTheme.typography.heading6,
                    modifier = Modifier.padding(contentPadding),
                )
            }

            else -> {
                ContentList(
                    listItems = { items ?: emptyList<Movie>().toImmutableList() },
                    onFocused = onFocused,
                    onClick = onClick,
                    onViewAllClick = onViewAllClick,
                    contentPadding = contentPadding,
                )
            }
        }
    }
}

@Composable
private fun ContentList(
    listItems: () -> ImmutableList<Movie>,
    onFocused: (Movie?) -> Unit,
    onClick: (Movie) -> Unit,
    onViewAllClick: () -> Unit,
    contentPadding: PaddingValues,
) {
    PositionFocusLazyRow(
        contentPadding = contentPadding,
    ) {
        items(
            items = listItems(),
            key = { it.ids.trakt.value },
        ) { item ->
            HorizontalMediaCard(
                title = item.title,
                containerImageUrl = item.images?.getFanartUrl(),
                contentImageUrl = item.images?.getLogoUrl(),
                paletteColor = item.colors?.colors?.second,
                onClick = { onClick(item) },
                footerContent = {
                    item.runtime?.let {
                        InfoChip(
                            text = it.inWholeMinutes.durationFormat(),
                        )
                    }
                },
                modifier = Modifier
                    .onFocusChanged {
                        if (it.isFocused) {
                            onFocused(item)
                        }
                    },
            )
        }

        if (listItems().size >= LISTS_SECTION_LIMIT) {
            item {
                HorizontalViewAllCard(
                    onClick = onViewAllClick,
                    modifier = Modifier
                        .onFocusChanged {
                            if (it.isFocused) {
                                onFocused(null)
                            }
                        },
                )
            }
        }

        emptyFocusListItems()
    }
}

@Preview(
    device = "id:tv_4k",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun ListsMoviesWatchlistViewPreview() {
    TraktTheme {
        ListsMoviesWatchlistView(
            items = null,
            isLoading = true,
            focusRequesters = mapOf("movies" to FocusRequester()),
            onFocused = {},
            onClick = {},
            onViewAllClick = {},
        )
    }
}
