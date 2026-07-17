@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package tv.trakt.trakt.core.summary.shows.features.seasons

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType.Companion.Confirm
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.ColorImage
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tv.trakt.trakt.LocalSnackbarState
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Idle
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.extensions.nowUtc
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Ids
import tv.trakt.trakt.common.model.Rating
import tv.trakt.trakt.common.model.Season
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.toSlugId
import tv.trakt.trakt.common.model.toTraktId
import tv.trakt.trakt.common.ui.composables.FilmProgressIndicator
import tv.trakt.trakt.core.summary.shows.features.context.episodes.EpisodeContextSheet
import tv.trakt.trakt.core.summary.shows.features.seasons.model.EpisodeItem
import tv.trakt.trakt.core.summary.shows.features.seasons.model.SeasonItem
import tv.trakt.trakt.core.summary.shows.features.seasons.model.ShowSeasons
import tv.trakt.trakt.core.summary.shows.features.seasons.ui.ShowEpisodesList
import tv.trakt.trakt.core.summary.shows.features.seasons.ui.ShowSeasonsList
import tv.trakt.trakt.core.summary.shows.features.seasons.watcheduntil.WatchedUntilSheet
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.components.confirmation.ConfirmationSheet
import tv.trakt.trakt.ui.components.confirmation.RemoveConfirmationSheet
import tv.trakt.trakt.ui.components.dateselection.DateSelectionSheet
import tv.trakt.trakt.ui.components.mediacards.skeletons.EpisodeSkeletonCard
import tv.trakt.trakt.ui.components.mediacards.skeletons.VerticalMediaSkeletonCard
import tv.trakt.trakt.ui.snackbar.SNACK_DURATION_SHORT
import tv.trakt.trakt.ui.theme.TraktTheme
import kotlin.time.Duration.Companion.milliseconds

@Composable
internal fun ShowSeasonsView(
    viewModel: ShowSeasonsViewModel,
    user: User?,
    headerPadding: PaddingValues,
    contentPadding: PaddingValues,
    onEpisodeClick: (episode: Episode) -> Unit,
    onAllSeasonsClick: (Int?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val snack = LocalSnackbarState.current

    val state by viewModel.state.collectAsStateWithLifecycle()

    val scope = rememberCoroutineScope()

    var confirmRemoveEpisodeSheet by remember { mutableStateOf<EpisodeItem?>(null) }
    var confirmMarkSeasonSheet by remember { mutableStateOf(false) }
    var confirmRemoveSeasonSheet by remember { mutableStateOf(false) }

    var episodeDateSheet by remember { mutableStateOf<EpisodeItem?>(null) }
    var episodeWatchedUntilSheet by remember { mutableStateOf<EpisodeItem?>(null) }
    var episodeContextSheet by remember { mutableStateOf<EpisodeItem?>(null) }
    var seasonDateSheet by remember { mutableStateOf(false) }

    ShowSeasonsContent(
        user = user,
        state = state,
        modifier = modifier,
        headerPadding = headerPadding,
        contentPadding = contentPadding,
        onEpisodeClick = {
            onEpisodeClick(it.episode)
        },
        onSeasonClick = {
            if (it.season.number == state.items.selectedSeason?.number) {
                onAllSeasonsClick(it.season.number)
            } else {
                viewModel.loadSeason(it)
            }
        },
        onCheckEpisodeClick = {
            viewModel.addToWatched(it.episode)
        },
        onCheckEpisodeLongClick = {
            episodeDateSheet = it
        },
        onMoreClick = {
            episodeContextSheet = it
        },
        onCheckSeasonClick = {
            confirmMarkSeasonSheet = true
        },
        onRemoveSeasonClick = {
            confirmRemoveSeasonSheet = true
        },
        onAllSeasonsClick = {
            onAllSeasonsClick(state.items.selectedSeason?.number)
        },
        onCollapse = viewModel::setCollapsed,
    )

    EpisodeContextSheet(
        episodeItem = episodeContextSheet,
        onTrackClick = {
            episodeDateSheet = it
        },
        onWatchedUntilClick = {
            episodeWatchedUntilSheet = it
        },
        onRemoveClick = {
            confirmRemoveEpisodeSheet = it
        },
        onDismiss = {
            episodeContextSheet = null
        },
    )

    WatchedUntilSheet(
        show = state.show,
        episode = episodeWatchedUntilSheet?.episode,
        onMarkAsWatched = {
        },
        onDismiss = {
            episodeWatchedUntilSheet = null
        },
    )

    ConfirmationSheet(
        active = confirmMarkSeasonSheet,
        onYes = {
            confirmMarkSeasonSheet = false
            seasonDateSheet = true
        },
        onNo = { confirmMarkSeasonSheet = false },
        title = stringResource(R.string.button_text_track),
        message = stringResource(
            R.string.warning_prompt_mark_as_watched_multiple_episodes,
            state.items.selectedSeasonEpisodes.count { !it.isWatched && it.episode.isReleased },
        ),
    )

    RemoveConfirmationSheet(
        active = confirmRemoveEpisodeSheet != null,
        onYes = {
            confirmRemoveEpisodeSheet?.let {
                viewModel.removeFromWatched(it.episode)
                confirmRemoveEpisodeSheet = null
            }
        },
        onNo = { confirmRemoveEpisodeSheet = null },
        title = stringResource(R.string.button_text_remove_from_history),
        message = stringResource(
            R.string.warning_prompt_remove_from_watched,
            "${confirmRemoveEpisodeSheet?.episode?.title}",
        ),
    )

    RemoveConfirmationSheet(
        active = confirmRemoveSeasonSheet,
        onYes = {
            confirmRemoveSeasonSheet = false
            state.items.selectedSeason?.let {
                viewModel.removeFromWatched(it)
            }
        },
        onNo = { confirmRemoveSeasonSheet = false },
        title = stringResource(R.string.button_text_remove_from_history),
        message = stringResource(
            R.string.warning_prompt_remove_from_watched,
            stringResource(
                R.string.text_season_number,
                state.items.selectedSeason?.number ?: 0,
            ),
        ),
    )

    DateSelectionSheet(
        active = episodeDateSheet != null,
        title = state.show?.title ?: "",
        onResult = { result ->
            episodeDateSheet?.let {
                viewModel.addToWatched(
                    episode = it.episode,
                    customDate = result,
                )
            }
        },
        onDismiss = {
            episodeDateSheet = null
        },
    )

    DateSelectionSheet(
        active = seasonDateSheet,
        title = state.show?.title ?: "",
        onResult = { result ->
            viewModel.addToWatched(
                season = state.items,
                customDate = result,
            )
        },
        onDismiss = {
            seasonDateSheet = false
        },
    )

    LaunchedEffect(state.info) {
        if (state.info == null) {
            return@LaunchedEffect
        }

        haptic.performHapticFeedback(Confirm)
        with(scope) {
            val job = launch {
                state.info?.get(context)?.let {
                    snack.showSnackbar(it)
                }
            }
            delay(SNACK_DURATION_SHORT.milliseconds)
            job.cancel()
        }

        viewModel.clearInfo()
    }
}

@Composable
private fun ShowSeasonsContent(
    state: ShowSeasonsState,
    user: User?,
    modifier: Modifier = Modifier,
    headerPadding: PaddingValues = PaddingValues(),
    contentPadding: PaddingValues = PaddingValues(),
    onEpisodeClick: ((EpisodeItem) -> Unit)? = null,
    onSeasonClick: ((SeasonItem) -> Unit)? = null,
    onCheckEpisodeClick: ((EpisodeItem) -> Unit)? = null,
    onCheckEpisodeLongClick: ((EpisodeItem) -> Unit)? = null,
    onMoreClick: ((EpisodeItem) -> Unit)? = null,
    onCheckSeasonClick: (() -> Unit)? = null,
    onRemoveSeasonClick: (() -> Unit)? = null,
    onAllSeasonsClick: (() -> Unit)? = null,
    onCollapse: (collapsed: Boolean) -> Unit = {},
) {
    var animateCollapse by rememberSaveable { mutableStateOf(false) }

    Column(
        verticalArrangement = spacedBy(TraktTheme.spacing.mainRowHeaderSpace),
        modifier = modifier
            .animateContentSize(
                animationSpec = if (animateCollapse) spring() else snap(),
            ),
    ) {
        val headerSeasons = stringResource(R.string.list_title_seasons)
        val headerCurrentSeason = state.items.selectedSeason?.let {
            when {
                it.isSpecial -> stringResource(R.string.text_season_specials)
                else -> stringResource(R.string.text_season_number, it.number)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(headerPadding),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = CenterVertically,
        ) {
            Row(
                horizontalArrangement = spacedBy(5.dp),
                verticalAlignment = CenterVertically,
                modifier = Modifier
                    .weight(1f)
                    .onClick(enabled = !state.loading.isLoading && state.collapsed != true) {
                        onAllSeasonsClick?.invoke()
                    },
            ) {
                TraktHeader(
                    title = headerSeasons,
                    titleColor = when {
                        !state.loading.isLoading && state.items.seasons.isEmpty() -> TraktTheme.colors.textPrimary
                        state.collapsed == true -> TraktTheme.colors.textPrimary
                        else -> TraktTheme.colors.textSecondary
                    },
                )

                if (state.items.selectedSeason != null && state.collapsed != true) {
                    TraktHeader(
                        title = "/",
                        titleColor = TraktTheme.colors.textSecondary,
                    )
                    TraktHeader(
                        title = headerCurrentSeason ?: "",
                    )
                }

                if (state.items.selectedSeason != null && state.collapsed != true) {
                    AllSeasonsDropdown(
                        state = state,
                        onSeasonClick = onSeasonClick,
                    )
                }

                if (user != null && state.collapsed != true && !state.loading.isLoading) {
                    Icon(
                        painter = painterResource(R.drawable.ic_chevron_right),
                        contentDescription = null,
                        tint = TraktTheme.colors.textPrimary,
                        modifier = Modifier
                            .padding(start = 7.dp)
                            .size(18.dp)
                            .onClick(enabled = !state.loadingSeason.isLoading) {
                                onAllSeasonsClick?.invoke()
                            },
                    )
                }
            }

            Row(
                horizontalArrangement = spacedBy(8.dp),
                verticalAlignment = CenterVertically,
            ) {
                if (state.loadingSeason.isLoading) {
                    FilmProgressIndicator(size = 16.dp)
                }

                Icon(
                    painter = painterResource(R.drawable.ic_arrow_dropdown),
                    contentDescription = null,
                    tint = TraktTheme.colors.textSecondary,
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .rotate(if (state.collapsed == true) -180F else 0F)
                        .size(16.dp)
                        .onClick {
                            animateCollapse = true
                            val current = (state.collapsed ?: false)
                            onCollapse(!current)
                        },
                )
            }
        }

        if (state.collapsed != true) {
            Crossfade(
                targetState = state.loading,
                animationSpec = tween(300),
            ) { loading ->
                when (loading) {
                    Idle, Loading -> {
                        ContentLoading(
                            visible = loading.isLoading,
                            contentPadding = contentPadding,
                        )
                    }

                    Done -> {
                        if (state.items.seasons.isEmpty()) {
                            ContentEmpty(
                                contentPadding = headerPadding,
                            )
                        } else {
                            ContentList(
                                show = state.show,
                                seasons = state.items,
                                contentPadding = contentPadding,
                                onEpisodeClick = onEpisodeClick,
                                onSeasonClick = onSeasonClick,
                                onSeasonLongClick = {
                                    if (state.loadingSeason.isLoading) {
                                        return@ContentList
                                    }
                                    when (state.items.isSelectedSeasonWatched) {
                                        true -> onRemoveSeasonClick?.invoke()
                                        else -> onCheckSeasonClick?.invoke()
                                    }
                                },
                                onCheckEpisodeClick = onCheckEpisodeClick,
                                onCheckEpisodeLongClick = onCheckEpisodeLongClick,
                                onMoreClick = onMoreClick,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AllSeasonsDropdown(
    state: ShowSeasonsState,
    onSeasonClick: ((SeasonItem) -> Unit)?,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .padding(start = 12.dp)
            .size(16.dp),
    ) {
        val seasonsMenuVisible = remember { mutableStateOf(false) }
        val dropdownScrollState = rememberScrollState()
        val density = LocalDensity.current

        LaunchedEffect(seasonsMenuVisible.value) {
            if (seasonsMenuVisible.value) {
                val index = state.items.seasons
                    .indexOfFirst { it.season.number == state.items.selectedSeason?.number }
                    .coerceAtLeast(0)
                if (index > 0) {
                    val itemHeightPx = with(density) { 48.dp.roundToPx() }
                    dropdownScrollState.scrollTo(index * itemHeightPx)
                }
            }
        }

        Icon(
            painter = painterResource(R.drawable.ic_chevron_all),
            contentDescription = null,
            tint = TraktTheme.colors.textPrimary,
            modifier = Modifier
                .size(16.dp)
                .onClick {
                    seasonsMenuVisible.value = true
                },
        )

        DropdownMenu(
            containerColor = TraktTheme.colors.dialogContainer,
            shape = RoundedCornerShape(20.dp),
            expanded = seasonsMenuVisible.value,
            scrollState = dropdownScrollState,
            onDismissRequest = {
                seasonsMenuVisible.value = false
            },
        ) {
            for (season in state.items.seasons) {
                val seasonTitle = when (season.season.number) {
                    0 -> stringResource(R.string.text_season_specials)
                    else -> stringResource(R.string.text_season_number, season.season.number)
                }
                DropdownMenuItem(
                    text = {
                        Text(
                            text = seasonTitle,
                            style = TraktTheme.typography.buttonTertiary,
                            color = when (season.season.number) {
                                state.items.selectedSeason?.number -> TraktTheme.colors.textPrimary
                                else -> TraktTheme.colors.textSecondary
                            },
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    },
                    onClick = {
                        seasonsMenuVisible.value = false
                        onSeasonClick?.invoke(season)
                    },
                )
            }
        }
    }
}

@Composable
private fun ContentList(
    show: Show?,
    seasons: ShowSeasons,
    contentPadding: PaddingValues,
    onSeasonClick: ((SeasonItem) -> Unit)? = null,
    onSeasonLongClick: ((SeasonItem) -> Unit)? = null,
    onEpisodeClick: ((EpisodeItem) -> Unit)? = null,
    onCheckEpisodeClick: ((EpisodeItem) -> Unit)? = null,
    onCheckEpisodeLongClick: ((EpisodeItem) -> Unit)? = null,
    onMoreClick: ((EpisodeItem) -> Unit)? = null,
) {
    Column(
        verticalArrangement = spacedBy(20.dp),
    ) {
        ShowSeasonsList(
            show = show,
            seasons = seasons.seasons,
            selectedSeason = seasons.selectedSeason?.number,
            snapScrollEnabled = true,
            onSeasonClick = onSeasonClick ?: {},
            onSeasonLongClick = onSeasonLongClick ?: {},
            contentPadding = contentPadding,
            modifier = Modifier.fillMaxWidth(),
        )

        ShowEpisodesList(
            isLoading = seasons.isSeasonLoading,
            show = show,
            season = seasons.selectedSeason?.number,
            episodes = seasons.selectedSeasonEpisodes,
            onEpisodeClick = onEpisodeClick ?: {},
            onCheckClick = onCheckEpisodeClick ?: {},
            onCheckLongClick = onCheckEpisodeLongClick ?: {},
            onMoreClick = onMoreClick ?: {},
            contentPadding = contentPadding,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun ContentLoading(
    visible: Boolean = true,
    contentPadding: PaddingValues,
) {
    Column(
        verticalArrangement = spacedBy(20.dp),
    ) {
        LazyRow(
            horizontalArrangement = spacedBy(TraktTheme.spacing.mainRowSpace),
            contentPadding = contentPadding,
            userScrollEnabled = false,
            modifier = Modifier
                .fillMaxWidth()
                .alpha(if (visible) 1F else 0F),
        ) {
            items(count = 12) {
                VerticalMediaSkeletonCard(
                    secondaryChip = true,
                )
            }
        }

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = spacedBy(TraktTheme.spacing.mainRowSpace),
            contentPadding = contentPadding,
        ) {
            items(count = 3) {
                EpisodeSkeletonCard()
            }
        }
    }
}

@Composable
private fun ContentEmpty(contentPadding: PaddingValues) {
    Text(
        text = stringResource(R.string.list_placeholder_empty),
        color = TraktTheme.colors.textSecondary,
        style = TraktTheme.typography.heading6,
        modifier = Modifier.padding(contentPadding),
    )
}

// -- Previews --

@OptIn(ExperimentalCoilApi::class)
@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview() {
    TraktTheme {
        val previewHandler = AsyncImagePreviewHandler {
            ColorImage(Color.Blue.toArgb())
        }
        CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
            ShowSeasonsContent(
                user = PreviewData.user1,
                state = ShowSeasonsState(
                    loading = Done,
                    items = ShowSeasons(
                        selectedSeasonEpisodes = listOf(
                            EpisodeItem(PreviewData.episode1),
                        ).toImmutableList(),
                        selectedSeason = Season(
                            ids = Ids(
                                trakt = 1.toTraktId(),
                                slug = "slug".toSlugId(),
                            ),
                            number = 2,
                            episodeCount = 12,
                            rating = Rating(
                                rating = 8.5f,
                                votes = 1000,
                            ),
                            images = null,
                            firstAired = nowUtc(),
                            updatedAt = nowUtc(),
                        ),
                    ),
                ),
            )
        }
    }
}

@OptIn(ExperimentalCoilApi::class)
@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview2() {
    TraktTheme {
        val previewHandler = AsyncImagePreviewHandler {
            ColorImage(Color.Blue.toArgb())
        }
        CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
            ShowSeasonsContent(
                user = PreviewData.user1,
                state = ShowSeasonsState(
                    loading = Loading,
                ),
            )
        }
    }
}

@OptIn(ExperimentalCoilApi::class)
@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview3() {
    TraktTheme {
        val previewHandler = AsyncImagePreviewHandler {
            ColorImage(Color.Blue.toArgb())
        }
        CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
            ShowSeasonsContent(
                user = PreviewData.user1,
                state = ShowSeasonsState(
                    loading = Done,
                ),
            )
        }
    }
}
