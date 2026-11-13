package tv.trakt.trakt.app.core.profile.sections.favorites.viewall

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Text
import tv.trakt.trakt.app.common.ui.GenericErrorView
import tv.trakt.trakt.app.common.ui.mediacards.VerticalMediaCard
import tv.trakt.trakt.app.core.details.ui.BackdropImage
import tv.trakt.trakt.app.core.lists.views.ListSortControls
import tv.trakt.trakt.app.core.profile.sections.favorites.FavoriteCardChip
import tv.trakt.trakt.app.core.profile.sections.favorites.model.FavoriteItem
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.sorting.Sorting
import tv.trakt.trakt.common.ui.composables.FilmProgressIndicator
import tv.trakt.trakt.resources.R

@Composable
internal fun ProfileFavoritesViewAllScreen(
    viewModel: ProfileFavoritesViewAllViewModel,
    onNavigateToShow: (TraktId) -> Unit,
    onNavigateToMovie: (TraktId) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ProfileFavoritesViewAllContent(
        state = state,
        onClick = {
            if (state.isLoading) {
                return@ProfileFavoritesViewAllContent
            }
            when (it) {
                is FavoriteItem.ShowItem -> onNavigateToShow(it.show.ids.trakt)
                is FavoriteItem.MovieItem -> onNavigateToMovie(it.movie.ids.trakt)
            }
        },
        onSortingChange = viewModel::setSorting,
    )
}

@Composable
private fun ProfileFavoritesViewAllContent(
    state: ProfileFavoritesViewAllState,
    modifier: Modifier = Modifier,
    onClick: (FavoriteItem) -> Unit,
    onSortingChange: (Sorting) -> Unit,
) {
    var focusedItem by remember { mutableStateOf<FavoriteItem?>(null) }
    var focusedItemKey by rememberSaveable { mutableStateOf<String?>(null) }
    val focusRequesters = remember { mutableMapOf<String, FocusRequester>() }

    LaunchedEffect(state.isLoading) {
        // Used when list is updated after user comes back and modifies favorites etc.
        if (state.isLoading) {
            focusedItem = null
            focusedItemKey = null
            focusRequesters.clear()
        }
    }

    Box(
        contentAlignment = Alignment.TopStart,
        modifier = modifier
            .fillMaxSize()
            .background(TraktTheme.colors.backgroundPrimary)
            .focusProperties {
                onEnter = {
                    focusRequesters[focusedItemKey]?.requestFocus()
                }
            },
    ) {
        BackdropImage(
            imageUrl = focusedItem?.fullFanartImage,
            saturation = 0F,
            crossfade = true,
        )

        val gridSpace = TraktTheme.spacing.mainGridSpace
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = TraktTheme.size.verticalMediaCardSize),
            horizontalArrangement = spacedBy(gridSpace),
            verticalArrangement = spacedBy(gridSpace * 2),
            contentPadding = PaddingValues(
                start = TraktTheme.spacing.mainContentStartSpace,
                end = TraktTheme.spacing.mainContentEndSpace,
                top = 30.dp,
                bottom = TraktTheme.spacing.mainContentVerticalSpace,
            ),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = spacedBy(16.dp),
                ) {
                    Text(
                        text = stringResource(R.string.list_title_favorites),
                        color = TraktTheme.colors.textPrimary,
                        style = TraktTheme.typography.heading4,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1F),
                    )
                    ListSortControls(
                        sorting = state.sorting,
                        loading = state.isLoading && !state.items.isNullOrEmpty(),
                        onSortingChange = onSortingChange,
                        modifier = Modifier.focusProperties {
                            down = state.items?.firstOrNull()
                                ?.let { focusRequesters[it.key] }
                                ?: FocusRequester.Default
                        },
                    )
                }
            }

            if (state.isLoading && state.items.isNullOrEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    FilmProgressIndicator(
                        modifier = Modifier.focusable(),
                    )
                }
            } else if (!state.items.isNullOrEmpty()) {
                items(
                    count = state.items.size,
                    key = { index -> state.items[index].key },
                ) { index ->
                    val item = state.items[index]
                    val focusRequester = focusRequesters.getOrPut(item.key) {
                        FocusRequester()
                    }

                    VerticalMediaCard(
                        title = item.title,
                        imageUrl = item.posterImage,
                        onClick = { onClick(item) },
                        chipContent = {
                            FavoriteCardChip(
                                item = item,
                                sorting = state.itemsSorting,
                            )
                        },
                        modifier = Modifier
                            .focusRequester(focusRequester)
                            .onFocusChanged {
                                if (it.isFocused) {
                                    focusedItem = item
                                    focusedItemKey = item.key
                                }
                            },
                    )
                }
            }
        }
    }

    if (state.error != null) {
        GenericErrorView(
            error = state.error,
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = TraktTheme.spacing.mainContentStartSpace,
                    end = TraktTheme.spacing.mainContentEndSpace,
                ),
        )
    }
}

@Preview(
    device = "id:tv_4k",
    showBackground = true,
    backgroundColor = 0xFF131517,
    heightDp = 1000,
)
@Composable
private fun Preview() {
    TraktTheme {
        ProfileFavoritesViewAllContent(
            state = ProfileFavoritesViewAllState(),
            onClick = {},
            onSortingChange = {},
        )
    }
}
