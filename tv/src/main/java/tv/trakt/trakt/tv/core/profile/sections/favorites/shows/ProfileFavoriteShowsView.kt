package tv.trakt.trakt.tv.core.profile.sections.favorites.shows

import InfoChip
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Text
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.tv.R
import tv.trakt.trakt.tv.common.model.TraktId
import tv.trakt.trakt.tv.common.ui.PositionFocusLazyRow
import tv.trakt.trakt.tv.common.ui.mediacards.HorizontalMediaCard
import tv.trakt.trakt.tv.common.ui.mediacards.HorizontalMediaSkeletonCard
import tv.trakt.trakt.tv.common.ui.mediacards.HorizontalViewAllCard
import tv.trakt.trakt.tv.core.profile.ProfileConfig.PROFILE_FAVORITES_SECTION_LIMIT
import tv.trakt.trakt.tv.core.shows.model.Show
import tv.trakt.trakt.tv.helpers.extensions.emptyFocusListItems
import tv.trakt.trakt.tv.ui.theme.TraktTheme

private val sections = listOf(
    "content",
)

@Composable
internal fun ProfileFavoriteShowsView(
    modifier: Modifier = Modifier,
    viewModel: ProfileFavoriteShowsViewModel = koinViewModel(),
    headerPadding: PaddingValues = PaddingValues(),
    contentPadding: PaddingValues = PaddingValues(),
    onFocused: (Show?) -> Unit = {},
    onShowClick: (TraktId) -> Unit,
    onViewAllClick: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val focusRequesters = remember {
        sections.associateBy(
            keySelector = { it },
            valueTransform = { FocusRequester() },
        )
    }

    ProfileFavoriteShowsContent(
        state = state,
        modifier = modifier,
        headerPadding = headerPadding,
        contentPadding = contentPadding,
        focusRequesters = focusRequesters,
        onFocused = onFocused,
        onViewAllClick = onViewAllClick,
        onClick = { onShowClick(it.ids.trakt) },
    )
}

@Composable
internal fun ProfileFavoriteShowsContent(
    state: ProfileFavoriteShowsState,
    modifier: Modifier = Modifier,
    headerPadding: PaddingValues = PaddingValues(),
    contentPadding: PaddingValues = PaddingValues(),
    focusRequesters: Map<String, FocusRequester> = emptyMap(),
    onFocused: (Show?) -> Unit = {},
    onClick: (Show) -> Unit = {},
    onViewAllClick: () -> Unit = {},
) {
    Column(
        verticalArrangement = spacedBy(TraktTheme.spacing.mainRowHeaderSpace),
        modifier = modifier,
    ) {
        Text(
            text = stringResource(R.string.header_favorite_shows),
            color = TraktTheme.colors.textPrimary,
            style = TraktTheme.typography.heading5,
            modifier = Modifier.padding(headerPadding),
        )

        when {
            state.isLoading -> {
                ContentLoadingList(
                    contentPadding = contentPadding,
                )
            }
            state.items?.isEmpty() == true -> {
                Text(
                    text = stringResource(R.string.info_generic_empty_list),
                    color = TraktTheme.colors.textSecondary,
                    style = TraktTheme.typography.heading6,
                    modifier = Modifier.padding(headerPadding),
                )
            }
            else -> {
                ContentList(
                    items = { state.items ?: emptyList<Show>().toImmutableList() },
                    onFocused = onFocused,
                    onClick = onClick,
                    onViewAllClick = onViewAllClick,
                    contentPadding = contentPadding,
                    focusRequesters = focusRequesters,
                )
            }
        }
    }
}

@Composable
private fun ContentList(
    items: () -> ImmutableList<Show>,
    onFocused: (Show?) -> Unit,
    onClick: (Show) -> Unit,
    onViewAllClick: () -> Unit,
    contentPadding: PaddingValues,
    focusRequesters: Map<String, FocusRequester> = emptyMap(),
) {
    PositionFocusLazyRow(
        contentPadding = contentPadding,
        modifier = Modifier.focusRequester(
            focusRequesters["content"] ?: FocusRequester.Default,
        ),
    ) {
        items(
            items = items(),
            key = { it.ids.trakt.value },
        ) { item ->
            ContentListItem(
                item = item,
                onClick = { onClick(item) },
                onFocused = onFocused,
            )
        }

        if (items().size >= PROFILE_FAVORITES_SECTION_LIMIT) {
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

@Composable
private fun ContentListItem(
    item: Show,
    onClick: () -> Unit,
    onFocused: (Show) -> Unit,
) {
    HorizontalMediaCard(
        title = item.title,
        containerImageUrl = item.images?.getFanartUrl(),
        contentImageUrl = item.images?.getLogoUrl(),
        paletteColor = item.colors?.colors?.second,
        onClick = onClick,
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

@Composable
private fun ContentLoadingList(contentPadding: PaddingValues) {
    PositionFocusLazyRow(
        contentPadding = contentPadding,
    ) {
        items(count = 10) {
            HorizontalMediaSkeletonCard()
        }
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
        ProfileFavoriteShowsContent(
            state = ProfileFavoriteShowsState(
                isLoading = false,
                items = emptyList<Show>().toImmutableList(),
            ),
        )
    }
}
