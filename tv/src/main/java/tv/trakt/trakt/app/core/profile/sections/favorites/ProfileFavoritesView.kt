package tv.trakt.trakt.app.core.profile.sections.favorites

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Text
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.app.common.ui.PositionFocusLazyRow
import tv.trakt.trakt.app.common.ui.mediacards.VerticalMediaCard
import tv.trakt.trakt.app.common.ui.mediacards.VerticalMediaSkeletonCard
import tv.trakt.trakt.app.common.ui.mediacards.VerticalViewAllCard
import tv.trakt.trakt.app.core.profile.ProfileConfig.PROFILE_FAVORITES_SECTION_LIMIT
import tv.trakt.trakt.app.core.profile.sections.favorites.model.FavoriteItem
import tv.trakt.trakt.app.helpers.extensions.emptyFocusListItems
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.common.core.user.UserCollectionState
import tv.trakt.trakt.common.helpers.extensions.rememberDurationFormat
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.resources.R

private val sections = listOf(
    "content",
)

@Composable
internal fun ProfileFavoritesView(
    modifier: Modifier = Modifier,
    viewModel: ProfileFavoritesViewModel = koinViewModel(),
    headerPadding: PaddingValues = PaddingValues(),
    contentPadding: PaddingValues = PaddingValues(),
    onLoaded: () -> Unit = {},
    onFocused: (FavoriteItem?) -> Unit = {},
    onShowClick: (TraktId) -> Unit,
    onMovieClick: (TraktId) -> Unit,
    onViewAllClick: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val focusRequesters = remember {
        sections.associateBy(
            keySelector = { it },
            valueTransform = { FocusRequester() },
        )
    }

    LaunchedEffect(state.isLoading) {
        if (!state.isLoading && state.items != null) {
            onLoaded()
        }
    }

    ProfileFavoritesContent(
        state = state,
        modifier = modifier,
        headerPadding = headerPadding,
        contentPadding = contentPadding,
        focusRequesters = focusRequesters,
        onFocused = onFocused,
        onViewAllClick = onViewAllClick,
        onClick = {
            when (it) {
                is FavoriteItem.ShowItem -> onShowClick(it.show.ids.trakt)
                is FavoriteItem.MovieItem -> onMovieClick(it.movie.ids.trakt)
            }
        },
    )
}

@Composable
internal fun ProfileFavoritesContent(
    state: ProfileFavoritesState,
    modifier: Modifier = Modifier,
    headerPadding: PaddingValues = PaddingValues(),
    contentPadding: PaddingValues = PaddingValues(),
    focusRequesters: Map<String, FocusRequester> = emptyMap(),
    onFocused: (FavoriteItem?) -> Unit = {},
    onClick: (FavoriteItem) -> Unit = {},
    onViewAllClick: () -> Unit = {},
) {
    Column(
        verticalArrangement = spacedBy(TraktTheme.spacing.mainRowHeaderSpace),
        modifier = modifier,
    ) {
        Text(
            text = stringResource(R.string.list_title_favorites),
            color = TraktTheme.colors.textPrimary,
            style = TraktTheme.typography.heading5,
            modifier = Modifier.padding(headerPadding),
        )

        when {
            state.isLoading -> {
                ContentLoadingList(
                    contentPadding = contentPadding,
                    onFocused = { onFocused(null) },
                )
            }

            state.items?.isEmpty() == true -> {
                Text(
                    text = stringResource(R.string.list_placeholder_empty),
                    color = TraktTheme.colors.textSecondary,
                    style = TraktTheme.typography.heading6,
                    modifier = Modifier.padding(headerPadding),
                )
            }

            else -> {
                ContentList(
                    items = { state.items ?: emptyList<FavoriteItem>().toImmutableList() },
                    collection = state.collection,
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
    items: () -> ImmutableList<FavoriteItem>,
    collection: UserCollectionState,
    onFocused: (FavoriteItem?) -> Unit,
    onClick: (FavoriteItem) -> Unit,
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
            key = { it.key },
        ) { item ->
            ContentListItem(
                item = item,
                collection = collection,
                onClick = { onClick(item) },
                onFocused = onFocused,
            )
        }

        if (items().size >= PROFILE_FAVORITES_SECTION_LIMIT) {
            item {
                VerticalViewAllCard(
                    width = TraktTheme.size.verticalMediaBigCardSize,
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
    item: FavoriteItem,
    collection: UserCollectionState,
    onClick: () -> Unit,
    onFocused: (FavoriteItem) -> Unit,
) {
    VerticalMediaCard(
        width = TraktTheme.size.verticalMediaBigCardSize,
        title = item.title,
        imageUrl = item.posterImage,
        watched = collection.isWatched(item.id, item.mediaType, item.airedEpisodes),
        watching = collection.isWatching(item.id, item.mediaType, item.airedEpisodes),
        watchlist = collection.isWatchlist(item.id, item.mediaType),
        onClick = onClick,
        chipContent = {
            FavoriteCardChip(item = item)
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
internal fun FavoriteCardChip(item: FavoriteItem) {
    val subtitle = when (item) {
        is FavoriteItem.ShowItem -> {
            val episodes = stringResource(
                R.string.tag_text_number_of_episodes,
                item.show.airedEpisodes,
            )
            item.show.year?.let { "$it  •  $episodes" } ?: episodes
        }

        is FavoriteItem.MovieItem -> {
            val duration = rememberDurationFormat(item.movie.runtime?.inWholeMinutes)
            "${item.movie.yearString}  •  $duration"
        }
    }

    Column(
        verticalArrangement = spacedBy(1.dp),
    ) {
        Text(
            text = item.title,
            style = TraktTheme.typography.cardTitle,
            color = TraktTheme.colors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = subtitle,
            style = TraktTheme.typography.cardSubtitle,
            color = TraktTheme.colors.textSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun ContentLoadingList(
    contentPadding: PaddingValues,
    onFocused: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PositionFocusLazyRow(
        contentPadding = contentPadding,
        modifier = modifier,
    ) {
        items(count = 10) {
            VerticalMediaSkeletonCard(
                width = TraktTheme.size.verticalMediaBigCardSize,
                modifier = Modifier
                    .padding(bottom = 10.dp)
                    .onFocusChanged {
                        if (it.isFocused) {
                            onFocused()
                        }
                    },
            )
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
        ProfileFavoritesContent(
            state = ProfileFavoritesState(
                isLoading = false,
                items = emptyList<FavoriteItem>().toImmutableList(),
            ),
        )
    }
}
