package tv.trakt.trakt.tv.core.lists.views

import InfoChip
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.tv.material3.Text
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.tv.R
import tv.trakt.trakt.tv.common.ui.PositionFocusLazyRow
import tv.trakt.trakt.tv.common.ui.mediacards.HorizontalMediaCard
import tv.trakt.trakt.tv.common.ui.mediacards.HorizontalViewAllCard
import tv.trakt.trakt.tv.core.lists.ListsConfig.LISTS_SECTION_LIMIT
import tv.trakt.trakt.tv.core.lists.ListsContentLoading
import tv.trakt.trakt.tv.core.shows.model.Show
import tv.trakt.trakt.tv.helpers.extensions.emptyFocusListItems
import tv.trakt.trakt.tv.ui.theme.TraktTheme

@Composable
internal fun ListsShowsWatchlistView(
    items: ImmutableList<Show>?,
    isLoading: Boolean,
    focusRequesters: Map<String, FocusRequester>,
    onFocused: (Show?) -> Unit,
    onClick: (Show) -> Unit,
    onViewAllClick: () -> Unit,
) {
    val contentPadding = PaddingValues(
        start = TraktTheme.spacing.mainContentStartSpace,
        end = TraktTheme.spacing.mainContentEndSpace,
    )

    var isFocusable by rememberSaveable { mutableStateOf(true) }

    LaunchedEffect(items) {
        if (items != null) {
            isFocusable = false
            focusRequesters
                .getValue("shows")
                .requestFocus()
        }
    }

    Column(
        verticalArrangement = spacedBy(TraktTheme.spacing.mainRowHeaderSpace),
        modifier = Modifier
            .focusGroup()
            .focusRequester(focusRequesters.getValue("shows")),
    ) {
        Text(
            text = stringResource(R.string.header_shows_watchlist),
            color = TraktTheme.colors.textPrimary,
            style = TraktTheme.typography.heading5,
            modifier = Modifier
                .padding(contentPadding)
                .focusRequester(focusRequesters["initial"] ?: FocusRequester.Default)
                .focusable(isFocusable),
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
                    listItems = { items ?: emptyList<Show>().toImmutableList() },
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
    listItems: () -> ImmutableList<Show>,
    onFocused: (Show?) -> Unit,
    onClick: (Show) -> Unit,
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
                    InfoChip(
                        text = stringResource(R.string.episodes_number, item.airedEpisodes),
                    )
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
