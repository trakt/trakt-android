package tv.trakt.trakt.app.core.lists.views

import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.tv.material3.Text
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import tv.trakt.trakt.app.common.ui.EmptyListCard
import tv.trakt.trakt.app.common.ui.PositionFocusLazyRow
import tv.trakt.trakt.app.common.ui.mediacards.VerticalMediaSkeletonCard
import tv.trakt.trakt.app.common.ui.mediacards.VerticalViewAllCard
import tv.trakt.trakt.app.core.details.lists.details.media.model.ListMediaItem
import tv.trakt.trakt.app.core.lists.ListsConfig.LISTS_SECTION_LIMIT
import tv.trakt.trakt.app.core.lists.ListsContentLoading
import tv.trakt.trakt.app.helpers.extensions.emptyFocusListVerticalItems
import tv.trakt.trakt.app.helpers.extensions.requestSafeFocus
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.common.core.user.UserCollectionState
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.sorting.Sorting
import tv.trakt.trakt.resources.R

internal const val WATCHLIST_SECTION = "watchlist"

@Composable
internal fun ListsWatchlistView(
    items: ImmutableList<ListMediaItem>?,
    collection: UserCollectionState,
    isLoading: Boolean,
    focusRequesters: Map<String, FocusRequester>,
    onFocused: (ListMediaItem?) -> Unit,
    onShowClick: (TraktId) -> Unit,
    onMovieClick: (TraktId) -> Unit,
    onViewAllClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLoaded: () -> Unit = {},
) {
    val contentPadding = PaddingValues(
        start = TraktTheme.spacing.mainContentStartSpace,
        end = TraktTheme.spacing.mainContentEndSpace,
    )
    val rowFocusRequester = focusRequesters.getValue(WATCHLIST_SECTION)

    LaunchedEffect(isLoading) {
        if (!isLoading && items != null) {
            onLoaded()
        }
    }

    Column(
        verticalArrangement = spacedBy(TraktTheme.spacing.mainRowHeaderSpace),
        modifier = modifier.focusGroup(),
    ) {
        Text(
            text = stringResource(R.string.list_title_watchlist),
            color = TraktTheme.colors.textPrimary,
            style = TraktTheme.typography.heading5,
            modifier = Modifier.padding(contentPadding),
        )

        Box(
            modifier = Modifier
                .focusGroup()
                .focusRequester(rowFocusRequester),
        ) {
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
                        items = items ?: persistentListOf(),
                        collection = collection,
                        onFocused = onFocused,
                        onShowClick = onShowClick,
                        onMovieClick = onMovieClick,
                        onViewAllClick = onViewAllClick,
                        contentPadding = contentPadding,
                    )
                }
            }
        }
    }
}

@Composable
private fun ContentList(
    items: ImmutableList<ListMediaItem>,
    collection: UserCollectionState,
    onFocused: (ListMediaItem?) -> Unit,
    onShowClick: (TraktId) -> Unit,
    onMovieClick: (TraktId) -> Unit,
    onViewAllClick: () -> Unit,
    contentPadding: PaddingValues,
) {
    val restoreItem = remember { FocusRequester() }
    var focusedKey by remember { mutableStateOf<String?>(null) }
    val restoreKey = focusedKey ?: items.firstOrNull()?.key

    PositionFocusLazyRow(
        contentPadding = contentPadding,
        modifier = Modifier
            // Entering from the wider list cards below would otherwise skip ahead.
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
            items = items,
            key = { it.key },
        ) { item ->
            ListMediaItemCard(
                item = item,
                collection = collection,
                sorting = Sorting.Default,
                enabled = true,
                onShowClick = onShowClick,
                onMovieClick = onMovieClick,
                onEpisodeClick = { _, _ -> },
                showWatchlist = false,
                modifier = Modifier
                    .then(
                        if (item.key == restoreKey) Modifier.focusRequester(restoreItem) else Modifier,
                    )
                    .onFocusChanged {
                        if (it.isFocused) {
                            focusedKey = item.key
                            onFocused(item)
                        }
                    },
            )
        }

        if (items.size >= LISTS_SECTION_LIMIT) {
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
private fun Preview() {
    TraktTheme {
        ListsWatchlistView(
            items = null,
            collection = UserCollectionState.Default,
            isLoading = true,
            focusRequesters = mapOf(WATCHLIST_SECTION to FocusRequester()),
            onFocused = {},
            onShowClick = {},
            onMovieClick = {},
            onViewAllClick = {},
        )
    }
}
