@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.home.sections.watchlist

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement.SpaceBetween
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType.Companion.Confirm
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.common.firebase.FirebaseConfig.RemoteKey.MOBILE_EMPTY_IMAGE_2
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.LoadingState.IDLE
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.helpers.extensions.durationFormat
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.ui.composables.FilmProgressIndicator
import tv.trakt.trakt.core.home.views.HomeEmptyView
import tv.trakt.trakt.core.lists.sections.watchlist.features.context.movies.sheets.WatchlistMovieSheet
import tv.trakt.trakt.core.lists.sections.watchlist.model.WatchlistItem
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.components.mediacards.VerticalMediaCard
import tv.trakt.trakt.ui.components.mediacards.skeletons.VerticalMediaSkeletonCard
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun HomeWatchlistView(
    modifier: Modifier = Modifier,
    viewModel: HomeWatchlistViewModel = koinViewModel(),
    headerPadding: PaddingValues,
    contentPadding: PaddingValues,
    onMovieClick: (TraktId) -> Unit,
    onMoviesClick: () -> Unit,
    onMoreClick: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current

    var contextSheet by remember { mutableStateOf<Movie?>(null) }

    LaunchedEffect(state) {
        state.navigateMovie?.let {
            viewModel.clearNavigation()
            onMovieClick(it)
        }
    }

    LaunchedEffect(state.info) {
        if (state.info != null) {
            haptic.performHapticFeedback(Confirm)
            viewModel.clearInfo()
        }
    }

    HomeWatchlistContent(
        state = state,
        modifier = modifier,
        headerPadding = headerPadding,
        contentPadding = contentPadding,
        onMoviesClick = onMoviesClick,
        onClick = {
            viewModel.navigateToMovie(it)
        },
        onLongClick = {
            contextSheet = it
        },
        onCheckClick = {
            viewModel.addToHistory(it.ids.trakt)
        },
        onMoreClick = onMoreClick,
    )

    WatchlistMovieSheet(
        addLocally = false,
        sheetItem = contextSheet,
        onDismiss = { contextSheet = null },
        onRemoveWatchlist = {
            viewModel.loadData(ignoreErrors = true)
        },
        onAddWatched = {
            viewModel.addToHistory(it.ids.trakt)
        },
    )
}

@Composable
internal fun HomeWatchlistContent(
    state: HomeWatchlistState,
    modifier: Modifier = Modifier,
    headerPadding: PaddingValues = PaddingValues(),
    contentPadding: PaddingValues = PaddingValues(),
    onMoviesClick: () -> Unit = {},
    onClick: (Movie) -> Unit = {},
    onLongClick: (Movie) -> Unit = {},
    onCheckClick: (Movie) -> Unit = {},
    onMoreClick: () -> Unit = {},
) {
    Column(
        verticalArrangement = spacedBy(TraktTheme.spacing.mainRowHeaderSpace),
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(headerPadding)
                .onClick(enabled = state.loading == DONE) {
                    onMoreClick()
                },
            horizontalArrangement = SpaceBetween,
            verticalAlignment = CenterVertically,
        ) {
            TraktHeader(
                title = stringResource(R.string.page_title_watchlist),
                subtitle = stringResource(R.string.list_description_released_movies),
            )
            if (!state.items.isNullOrEmpty() || state.loading != DONE) {
                Icon(
                    painter = painterResource(R.drawable.ic_chevron_right),
                    contentDescription = null,
                    tint = TraktTheme.colors.textPrimary,
                    modifier = Modifier
                        .size(20.dp)
                        .graphicsLayer {
                            translationX = (4.9).dp.toPx()
                        },
                )
            }
        }

        Crossfade(
            targetState = state.loading,
            animationSpec = tween(200),
        ) { loading ->
            when (loading) {
                IDLE, LOADING -> {
                    ContentLoadingList(
                        visible = loading.isLoading,
                        contentPadding = contentPadding,
                    )
                }
                DONE -> {
                    when {
                        state.error != null -> {
                            Text(
                                text =
                                    "${stringResource(R.string.error_text_unexpected_error_short)}\n\n${state.error}",
                                color = TraktTheme.colors.textSecondary,
                                style = TraktTheme.typography.meta,
                                maxLines = 10,
                                modifier = Modifier.padding(contentPadding),
                            )
                        }
                        state.items?.isEmpty() == true -> {
                            val imageUrl = remember {
                                Firebase.remoteConfig.getString(MOBILE_EMPTY_IMAGE_2).ifBlank { null }
                            }
                            HomeEmptyView(
                                text = stringResource(R.string.text_cta_watchlist_released),
                                icon = R.drawable.ic_empty_watchlist,
                                buttonText = stringResource(R.string.button_label_browse_movies),
                                backgroundImageUrl = imageUrl,
                                backgroundImage = if (imageUrl == null) R.drawable.ic_splash_background_2 else null,
                                onClick = onMoviesClick,
                                height = (226.25).dp,
                                modifier = Modifier
                                    .padding(contentPadding),
                            )
                        }
                        else -> {
                            ContentList(
                                listItems = (state.items ?: emptyList()).toImmutableList(),
                                contentPadding = contentPadding,
                                onClick = onClick,
                                onLongClick = onLongClick,
                                onCheckClick = onCheckClick,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ContentLoadingList(
    visible: Boolean = true,
    contentPadding: PaddingValues,
) {
    LazyRow(
        horizontalArrangement = spacedBy(TraktTheme.spacing.mainRowSpace),
        contentPadding = contentPadding,
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (visible) 1F else 0F),
    ) {
        items(count = 6) {
            VerticalMediaSkeletonCard(
                chipRatio = 0.66F,
                modifier = Modifier
                    .padding(bottom = 4.dp),
            )
        }
    }
}

@Composable
private fun ContentList(
    listItems: ImmutableList<WatchlistItem.MovieItem>,
    listState: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues,
    onClick: (Movie) -> Unit,
    onLongClick: (Movie) -> Unit,
    onCheckClick: (Movie) -> Unit,
) {
    LazyRow(
        state = listState,
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = spacedBy(TraktTheme.spacing.mainRowSpace),
        contentPadding = contentPadding,
    ) {
        items(
            items = listItems,
            key = { it.movie.ids.trakt.value },
        ) { item ->
            ContentListItem(
                item = item,
                onClick = { onClick(item.movie) },
                onLongClick = { onLongClick(item.movie) },
                onCheckClick = { onCheckClick(item.movie) },
                modifier = Modifier.animateItem(
                    fadeInSpec = null,
                    fadeOutSpec = null,
                ),
            )
        }
    }
}

@Composable
private fun ContentListItem(
    item: WatchlistItem.MovieItem,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
    onCheckClick: () -> Unit = {},
) {
    VerticalMediaCard(
        title = item.movie.title,
        imageUrl = item.movie.images?.getPosterUrl(),
        onClick = onClick,
        onLongClick = onLongClick,
        chipContent = {
            Row(
                verticalAlignment = CenterVertically,
                horizontalArrangement = SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                val footerText = remember {
                    val runtime = item.movie.runtime?.inWholeMinutes
                    if (runtime != null) {
                        "${item.movie.year} • ${runtime.durationFormat()}"
                    } else {
                        item.movie.year.toString()
                    }
                }
                Text(
                    text = footerText,
                    style = TraktTheme.typography.cardTitle,
                    color = TraktTheme.colors.textPrimary,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1F, fill = false),
                )

                val checkSize = 20.dp
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .padding(start = 8.dp, end = 2.dp)
                        .size(checkSize),
                ) {
                    if (item.loading) {
                        FilmProgressIndicator(size = checkSize - 3.dp)
                    } else {
                        Icon(
                            painter = painterResource(R.drawable.ic_check),
                            contentDescription = null,
                            tint = TraktTheme.colors.accent,
                            modifier = Modifier
                                .size(checkSize)
                                .onClick { onCheckClick() },
                        )
                    }
                }
            }
        },
        modifier = modifier,
    )
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview() {
    TraktTheme {
        HomeWatchlistContent(
            state = HomeWatchlistState(
                loading = IDLE,
            ),
        )
    }
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview2() {
    TraktTheme {
        HomeWatchlistContent(
            state = HomeWatchlistState(
                loading = LOADING,
            ),
        )
    }
}
