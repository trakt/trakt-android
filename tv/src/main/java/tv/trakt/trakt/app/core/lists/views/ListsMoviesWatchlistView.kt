package tv.trakt.trakt.app.core.lists.views

import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.app.common.ui.EmptyListCard
import tv.trakt.trakt.app.common.ui.PositionFocusLazyRow
import tv.trakt.trakt.app.common.ui.mediacards.VerticalMediaCard
import tv.trakt.trakt.app.common.ui.mediacards.VerticalMediaSkeletonCard
import tv.trakt.trakt.app.common.ui.mediacards.VerticalViewAllCard
import tv.trakt.trakt.app.core.lists.ListsConfig.LISTS_SECTION_LIMIT
import tv.trakt.trakt.app.core.lists.ListsContentLoading
import tv.trakt.trakt.app.helpers.extensions.emptyFocusListVerticalItems
import tv.trakt.trakt.app.helpers.extensions.requestSafeFocus
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.common.helpers.extensions.rememberDurationFormat
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.resources.R

@Composable
internal fun ListsMoviesWatchlistView(
    items: ImmutableList<Movie>?,
    isLoading: Boolean,
    focusRequesters: Map<String, FocusRequester>,
    onLoaded: () -> Unit = {},
    onFocused: (Movie?) -> Unit,
    onClick: (Movie) -> Unit,
    onViewAllClick: () -> Unit,
) {
    val contentPadding = PaddingValues(
        start = TraktTheme.spacing.mainContentStartSpace,
        end = TraktTheme.spacing.mainContentEndSpace,
    )

    LaunchedEffect(isLoading) {
        if (!isLoading && items != null) {
            onLoaded()
        }
    }

    Column(
        verticalArrangement = spacedBy(TraktTheme.spacing.mainRowHeaderSpace),
        modifier = Modifier
            .focusGroup()
            .focusRequester(focusRequesters.getValue("movies")),
    ) {
        Text(
            text = stringResource(R.string.list_title_watchlist_movies),
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
                EmptyListCard(
                    modifier = Modifier.padding(contentPadding),
                    onFocused = { onFocused(null) },
                ) {
                    VerticalMediaSkeletonCard()
                }
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
    val restoreItem = remember { FocusRequester() }
    var focusedId by remember { mutableStateOf<Int?>(null) }
    val restoreId = focusedId ?: listItems().firstOrNull()?.ids?.trakt?.value

    PositionFocusLazyRow(
        contentPadding = contentPadding,
        modifier = Modifier
            // Entering from the wider personal list cards below would otherwise skip ahead.
            .focusProperties {
                onEnter = {
                    if (requestedFocusDirection == FocusDirection.Up) {
                        restoreItem.requestSafeFocus()
                    }
                }
            }
            .focusGroup(),
    ) {
        items(
            items = listItems(),
            key = { it.ids.trakt.value },
        ) { item ->
            VerticalMediaCard(
                title = item.title,
                imageUrl = item.images?.getPosterUrl(),
                onClick = { onClick(item) },
                chipContent = {
                    Column(
                        verticalArrangement = spacedBy(1.dp),
                    ) {
                        Text(
                            text = item.yearString +
                                "  •  ${rememberDurationFormat(item.runtime?.inWholeMinutes)}",
                            style = TraktTheme.typography.cardTitle,
                            color = TraktTheme.colors.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                },
                modifier = Modifier
                    .then(
                        if (item.ids.trakt.value == restoreId) Modifier.focusRequester(restoreItem) else Modifier,
                    )
                    .onFocusChanged {
                        if (it.isFocused) {
                            focusedId = item.ids.trakt.value
                            onFocused(item)
                        }
                    },
            )
        }

        if (listItems().size >= LISTS_SECTION_LIMIT) {
            item {
                VerticalViewAllCard(
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

        emptyFocusListVerticalItems()
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
