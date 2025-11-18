@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.home.sections.watchlist

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
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
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.ui.composables.FilmProgressIndicator
import tv.trakt.trakt.core.home.views.HomeEmptyView
import tv.trakt.trakt.core.lists.sections.watchlist.features.context.movies.sheets.WatchlistMovieSheet
import tv.trakt.trakt.core.lists.sections.watchlist.features.context.shows.sheets.WatchlistShowSheet
import tv.trakt.trakt.core.lists.sections.watchlist.model.WatchlistItem
import tv.trakt.trakt.core.lists.sections.watchlist.model.WatchlistItem.MovieItem
import tv.trakt.trakt.core.lists.sections.watchlist.model.WatchlistItem.ShowItem
import tv.trakt.trakt.core.main.model.MediaMode
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.components.dateselection.CustomDate
import tv.trakt.trakt.ui.components.dateselection.DateSelectionSheet
import tv.trakt.trakt.ui.components.dateselection.Now
import tv.trakt.trakt.ui.components.dateselection.ReleaseDate
import tv.trakt.trakt.ui.components.dateselection.UnknownDate
import tv.trakt.trakt.ui.components.mediacards.VerticalMediaCard
import tv.trakt.trakt.ui.components.mediacards.skeletons.VerticalMediaSkeletonCard
import tv.trakt.trakt.ui.theme.TraktTheme
import java.time.Instant
import java.time.ZoneOffset.UTC

@Composable
internal fun HomeWatchlistView(
    modifier: Modifier = Modifier,
    viewModel: HomeWatchlistViewModel = koinViewModel(),
    headerPadding: PaddingValues,
    contentPadding: PaddingValues,
    onShowClick: (TraktId) -> Unit,
    onShowsClick: () -> Unit,
    onMovieClick: (TraktId) -> Unit,
    onMoviesClick: () -> Unit,
    onMoreClick: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current

    var contextShowSheet by remember { mutableStateOf<ShowItem?>(null) }
    var contextMovieSheet by remember { mutableStateOf<MovieItem?>(null) }
    var dateSheet by remember { mutableStateOf<WatchlistItem?>(null) }

    LaunchedEffect(state) {
        state.navigateShow?.let {
            viewModel.clearNavigation()
            onShowClick(it)
        }
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
        onEmptyClick = {
            when (state.filter) {
                MediaMode.MOVIES -> onMoviesClick()
                else -> onShowsClick()
            }
        },
        onClick = {
            when (it) {
                is ShowItem -> viewModel.navigateToShow(it.show)
                is MovieItem -> viewModel.navigateToMovie(it.movie)
            }
        },
        onLongClick = {
            when (it) {
                is ShowItem -> contextShowSheet = it
                is MovieItem -> contextMovieSheet = it
            }
        },
        onCheckClick = {
            when (it) {
                is ShowItem -> viewModel.addShowToHistory(
                    showId = it.id,
                    episodeId = it.progress?.nextEpisode?.ids?.trakt,
                )
                is MovieItem -> viewModel.addMovieToHistory(it.id)
            }
        },
        onCheckLongClick = {
            dateSheet = it
        },
        onMoreClick = onMoreClick,
    )

    WatchlistShowSheet(
        addLocally = false,
        sheetItem = contextShowSheet?.show,
        onDismiss = {
            contextShowSheet = null
        },
        onAddWatched = {
            dateSheet = contextShowSheet
        },
        onRemoveWatchlist = {
            viewModel.loadData(ignoreErrors = true)
        },
    )

    WatchlistMovieSheet(
        addLocally = false,
        skipSnack = true,
        sheetItem = contextMovieSheet?.movie,
        onDismiss = {
            contextMovieSheet = null
        },
        onAddWatched = {
            dateSheet = contextMovieSheet
        },
        onRemoveWatchlist = {
            viewModel.loadData(ignoreErrors = true)
        },
    )

    HomeDateSelectionSheet(
        item = dateSheet,
        onDateSelected = { date ->
            dateSheet?.let {
                when (it) {
                    is MovieItem -> {
                        viewModel.addMovieToHistory(
                            movieId = it.id,
                            customDate = date,
                        )
                    }
                    is ShowItem -> {
                        val episode = (dateSheet as ShowItem).progress?.nextEpisode
                            ?: return@HomeDateSelectionSheet

                        viewModel.addShowToHistory(
                            showId = it.id,
                            episodeId = episode.ids.trakt,
                            customDate = date,
                        )
                    }
                }
            }
        },
        onDismiss = {
            dateSheet = null
        },
    )
}

@Composable
internal fun HomeWatchlistContent(
    state: HomeWatchlistState,
    modifier: Modifier = Modifier,
    headerPadding: PaddingValues = PaddingValues(),
    contentPadding: PaddingValues = PaddingValues(),
    onClick: (WatchlistItem) -> Unit = {},
    onLongClick: (WatchlistItem) -> Unit = {},
    onCheckClick: (WatchlistItem) -> Unit = {},
    onCheckLongClick: (WatchlistItem) -> Unit = {},
    onEmptyClick: () -> Unit = {},
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
                title = stringResource(R.string.list_title_from_watchlist),
                subtitle = stringResource(
                    when (state.filter) {
                        MediaMode.SHOWS -> R.string.list_description_released_shows
                        MediaMode.MOVIES -> R.string.list_description_released_movies
                        else -> R.string.list_description_released_media
                    },
                ),
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
                                buttonText = when (state.filter) {
                                    MediaMode.MOVIES -> stringResource(R.string.button_label_browse_movies)
                                    else -> stringResource(R.string.button_label_browse_shows)
                                },
                                backgroundImageUrl = imageUrl,
                                backgroundImage = when (imageUrl) {
                                    null -> R.drawable.ic_splash_background_2
                                    else -> null
                                },
                                onClick = onEmptyClick,
                                height = (226.25).dp,
                                modifier = Modifier
                                    .padding(contentPadding)
                                    .padding(bottom = 6.dp),
                            )
                        }
                        else -> {
                            ContentList(
                                listFilter = state.filter,
                                listItems = (state.items ?: emptyList()).toImmutableList(),
                                contentPadding = contentPadding,
                                onClick = onClick,
                                onLongClick = onLongClick,
                                onCheckClick = onCheckClick,
                                onCheckLongClick = onCheckLongClick,
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
    modifier: Modifier = Modifier,
    visible: Boolean = true,
    contentPadding: PaddingValues,
) {
    LazyRow(
        horizontalArrangement = spacedBy(TraktTheme.spacing.mainRowSpace),
        contentPadding = contentPadding,
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (visible) 1F else 0F),
    ) {
        items(count = 6) {
            VerticalMediaSkeletonCard(
                chip = true,
                secondaryChip = true,
                chipRatio = 0.5F,
                secondaryChipRatio = 0.66F,
            )
        }
    }
}

@Composable
private fun ContentList(
    listFilter: MediaMode?,
    listItems: ImmutableList<WatchlistItem>,
    listState: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues,
    onClick: (WatchlistItem) -> Unit,
    onLongClick: (WatchlistItem) -> Unit,
    onCheckClick: (WatchlistItem) -> Unit,
    onCheckLongClick: (WatchlistItem) -> Unit,
) {
    val currentFilter = remember { mutableStateOf(listFilter) }

    LaunchedEffect(listFilter) {
        if (currentFilter.value != listFilter) {
            currentFilter.value = listFilter
            listState.animateScrollToItem(0)
        }
    }

    LazyRow(
        state = listState,
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = spacedBy(TraktTheme.spacing.mainRowSpace),
        contentPadding = contentPadding,
    ) {
        items(
            items = listItems,
            key = { it.key },
        ) { item ->
            if (item is ShowItem) {
                ContentListShowItem(
                    item = item,
                    onClick = { onClick(item) },
                    onLongClick = { onLongClick(item) },
                    onCheckClick = { onCheckClick(item) },
                    onCheckLongClick = { onCheckLongClick(item) },
                    modifier = Modifier.animateItem(
                        fadeInSpec = null,
                        fadeOutSpec = null,
                    ),
                )
            }
            if (item is MovieItem) {
                ContentListMovieItem(
                    item = item,
                    onClick = { onClick(item) },
                    onLongClick = { onLongClick(item) },
                    onCheckClick = { onCheckClick(item) },
                    onCheckLongClick = { onCheckLongClick(item) },
                    modifier = Modifier.animateItem(
                        fadeInSpec = null,
                        fadeOutSpec = null,
                    ),
                )
            }
        }
    }
}

@Composable
private fun ContentListShowItem(
    item: ShowItem,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
    onCheckClick: () -> Unit = {},
    onCheckLongClick: () -> Unit = {},
) {
    VerticalMediaCard(
        title = item.title,
        imageUrl = item.images?.getPosterUrl(),
        onClick = onClick,
        onLongClick = onLongClick,
        chipContent = {
            Row(
                verticalAlignment = CenterVertically,
                horizontalArrangement = SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    verticalArrangement = spacedBy(1.dp),
                    modifier = Modifier
                        .onClick(onClick = onClick)
                        .weight(1F, fill = false),
                ) {
                    Text(
                        text = item.title,
                        style = TraktTheme.typography.cardTitle,
                        color = TraktTheme.colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    item.progress?.let {
                        Text(
                            text = item.progress.nextEpisode.seasonEpisodeString(),
                            style = TraktTheme.typography.cardSubtitle,
                            color = TraktTheme.colors.textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                ContentItemCheck(
                    isLoading = item.loading,
                    onCheckClick = onCheckClick,
                    onCheckLongClick = onCheckLongClick,
                    modifier = Modifier.padding(start = 4.dp),
                )
            }
        },
        modifier = modifier,
    )
}

@Composable
private fun ContentListMovieItem(
    item: MovieItem,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
    onCheckClick: () -> Unit = {},
    onCheckLongClick: () -> Unit = {},
) {
    VerticalMediaCard(
        title = item.title,
        imageUrl = item.images?.getPosterUrl(),
        onClick = onClick,
        onLongClick = onLongClick,
        chipContent = {
            Row(
                verticalAlignment = CenterVertically,
                horizontalArrangement = SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                val runtimeText = remember {
                    val runtime = item.movie.runtime?.inWholeMinutes?.durationFormat()
                    runtime ?: "TBA"
                }

                Column(
                    verticalArrangement = spacedBy(1.dp),
                    modifier = Modifier
                        .onClick(onClick = onClick)
                        .weight(1F, fill = false),
                ) {
                    Text(
                        text = item.title,
                        style = TraktTheme.typography.cardTitle,
                        color = TraktTheme.colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    Text(
                        text = runtimeText,
                        style = TraktTheme.typography.cardSubtitle,
                        color = TraktTheme.colors.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                ContentItemCheck(
                    isLoading = item.loading,
                    onCheckClick = onCheckClick,
                    onCheckLongClick = onCheckLongClick,
                    modifier = Modifier.padding(start = 4.dp),
                )
            }
        },
        modifier = modifier,
    )
}

@Composable
private fun ContentItemCheck(
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    checkSize: Dp = 20.dp,
    onCheckClick: () -> Unit,
    onCheckLongClick: () -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .padding(start = 8.dp, end = 2.dp)
            .size(checkSize),
    ) {
        if (isLoading) {
            FilmProgressIndicator(size = checkSize - 3.dp)
        } else {
            Icon(
                painter = painterResource(R.drawable.ic_check),
                contentDescription = null,
                tint = TraktTheme.colors.accent,
                modifier = Modifier
                    .size(checkSize)
                    .combinedClickable(
                        onClick = onCheckClick,
                        onLongClick = onCheckLongClick,
                        interactionSource = null,
                        indication = null,
                    ),
            )
        }
    }
}

@Composable
private fun HomeDateSelectionSheet(
    item: WatchlistItem?,
    onDateSelected: (Instant?) -> Unit,
    onDismiss: () -> Unit,
) {
    DateSelectionSheet(
        active = item != null,
        title = item?.title.orEmpty(),
        subtitle = when (item) {
            is ShowItem -> item.progress?.nextEpisode?.seasonEpisodeString()
            is MovieItem -> null
            else -> null
        },
        onResult = {
            if (item == null) return@DateSelectionSheet
            when (it) {
                is Now -> onDateSelected(null)
                is CustomDate -> onDateSelected(it.date)
                is UnknownDate -> onDateSelected(it.date)
                is ReleaseDate -> when (item) {
                    is ShowItem -> onDateSelected(
                        item.progress?.nextEpisode?.firstAired?.toInstant()
                            ?: item.show.released?.toInstant(),
                    )
                    is MovieItem -> {
                        val localDate = item.movie.released
                        val instantDate = localDate?.atTime(20, 0)?.toInstant(UTC)
                        onDateSelected(instantDate)
                    }
                }
            }
        },
        onDismiss = onDismiss,
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
