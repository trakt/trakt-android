package tv.trakt.trakt.app.core.search.views

import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.tv.material3.Text
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.app.common.ui.InfoChip
import tv.trakt.trakt.app.common.ui.PositionFocusLazyRow
import tv.trakt.trakt.app.common.ui.mediacards.HorizontalMediaCard
import tv.trakt.trakt.app.helpers.extensions.emptyFocusListItems
import tv.trakt.trakt.app.helpers.extensions.requestSafeFocus
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.resources.R

@Composable
internal fun SearchShowsView(
    header: String,
    items: ImmutableList<Show>?,
    focusRequesters: Map<String, FocusRequester>,
    onFocused: (Show?) -> Unit,
    onClick: (Show) -> Unit,
) {
    val state = rememberLazyListState()
    var currentItems by rememberSaveable { mutableStateOf<Int?>(null) }

    LaunchedEffect(items) {
        val itemsHash = items.hashCode()
        if (itemsHash != currentItems) {
            currentItems = itemsHash
            state.scrollToItem(0)
        }
    }

    val contentPadding = PaddingValues(
        start = TraktTheme.spacing.mainContentStartSpace,
        end = TraktTheme.spacing.mainContentEndSpace,
    )

    Column(
        verticalArrangement = spacedBy(TraktTheme.spacing.mainRowHeaderSpace),
        modifier = Modifier
            .focusProperties {
                onEnter = {
                    focusRequesters.getValue("shows").requestSafeFocus()
                }
            }
            .focusGroup()
            .focusable(),
    ) {
        Text(
            text = header,
            color = TraktTheme.colors.textPrimary,
            style = TraktTheme.typography.heading5,
            modifier = Modifier.padding(contentPadding),
        )

        ContentList(
            items = { items ?: emptyList<Show>().toImmutableList() },
            state = state,
            onFocused = onFocused,
            onClick = onClick,
            contentPadding = contentPadding,
            focusRequesters = focusRequesters,
        )
    }
}

@Composable
private fun ContentList(
    items: () -> ImmutableList<Show>,
    state: LazyListState,
    onFocused: (Show?) -> Unit,
    onClick: (Show) -> Unit,
    contentPadding: PaddingValues,
    focusRequesters: Map<String, FocusRequester>,
) {
    PositionFocusLazyRow(
        state = state,
        contentPadding = contentPadding,
        modifier = Modifier
            .focusRequester(focusRequesters.getValue("shows")),
    ) {
        itemsIndexed(
            items = items(),
            key = { _, item -> item.ids.trakt.value },
        ) { index, item ->
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

        emptyFocusListItems()
    }
}
