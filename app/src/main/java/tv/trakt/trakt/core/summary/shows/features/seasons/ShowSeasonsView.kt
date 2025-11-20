@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package tv.trakt.trakt.core.summary.shows.features.seasons

import androidx.compose.animation.Crossfade
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType.Companion.Confirm
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.LoadingState.IDLE
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.helpers.extensions.nowUtc
import tv.trakt.trakt.common.helpers.extensions.onClickCombined
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Ids
import tv.trakt.trakt.common.model.Season
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.toSlugId
import tv.trakt.trakt.common.model.toTraktId
import tv.trakt.trakt.common.ui.composables.FilmProgressIndicator
import tv.trakt.trakt.core.summary.shows.features.seasons.model.EpisodeItem
import tv.trakt.trakt.core.summary.shows.features.seasons.model.ShowSeasons
import tv.trakt.trakt.core.summary.shows.features.seasons.ui.ShowEpisodesList
import tv.trakt.trakt.core.summary.shows.features.seasons.ui.ShowSeasonsList
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.components.confirmation.ConfirmationSheet
import tv.trakt.trakt.ui.components.dateselection.DateSelectionSheet
import tv.trakt.trakt.ui.components.mediacards.skeletons.EpisodeSkeletonCard
import tv.trakt.trakt.ui.components.mediacards.skeletons.VerticalMediaSkeletonCard
import tv.trakt.trakt.ui.snackbar.SNACK_DURATION_SHORT
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun ShowSeasonsView(
    viewModel: ShowSeasonsViewModel,
    user: User?,
    headerPadding: PaddingValues,
    contentPadding: PaddingValues,
    onEpisodeClick: (episode: Episode) -> Unit,
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
    var seasonDateSheet by remember { mutableStateOf(false) }

    ShowSeasonsContent(
        state = state,
        user = user,
        modifier = modifier,
        headerPadding = headerPadding,
        contentPadding = contentPadding,
        onEpisodeClick = { onEpisodeClick(it.episode) },
        onSeasonClick = viewModel::loadSeason,
        onCheckEpisodeClick = {
            viewModel.addToWatched(it.episode)
        },
        onCheckEpisodeLongClick = {
            episodeDateSheet = it
        },
        onRemoveEpisodeClick = {
            confirmRemoveEpisodeSheet = it
        },
        onCheckSeasonClick = {
            confirmMarkSeasonSheet = true
        },
        onRemoveSeasonClick = {
            confirmRemoveSeasonSheet = true
        },
    )

    ConfirmationSheet(
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

    ConfirmationSheet(
        active = confirmMarkSeasonSheet,
        onYes = {
            confirmMarkSeasonSheet = false
            seasonDateSheet = true
        },
        onNo = { confirmMarkSeasonSheet = false },
        title = stringResource(R.string.button_text_mark_as_watched),
        message = stringResource(
            R.string.warning_prompt_mark_as_watched_multiple_episodes,
            state.items.selectedSeasonEpisodes.count { !it.isWatched },
        ),
    )

    ConfirmationSheet(
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
            delay(SNACK_DURATION_SHORT)
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
    onSeasonClick: ((Season) -> Unit)? = null,
    onCheckEpisodeClick: ((EpisodeItem) -> Unit)? = null,
    onCheckEpisodeLongClick: ((EpisodeItem) -> Unit)? = null,
    onRemoveEpisodeClick: ((EpisodeItem) -> Unit)? = null,
    onCheckSeasonClick: (() -> Unit)? = null,
    onRemoveSeasonClick: (() -> Unit)? = null,
) {
    Column(
        verticalArrangement = spacedBy(TraktTheme.spacing.mainRowHeaderSpace),
        modifier = modifier,
    ) {
        val headerSeasons =
            stringResource(R.string.list_title_seasons)

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
            ) {
                TraktHeader(
                    title = headerSeasons,
                    titleColor = TraktTheme.colors.textSecondary,
                )
                if (state.items.selectedSeason != null) {
                    TraktHeader(
                        title = "/",
                        titleColor = TraktTheme.colors.textSecondary,
                    )
                    TraktHeader(
                        title = headerCurrentSeason ?: "",
                    )
                }
            }

            if (user != null && state.items.isSelectedSeasonReleased) {
                val checkSize = 20.dp
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(checkSize),
                ) {
                    if (state.loadingSeason.isLoading) {
                        FilmProgressIndicator(size = checkSize - 3.dp)
                    } else {
                        val isLoading =
                            state.items.isSeasonLoading ||
                                state.loadingSeason.isLoading

                        if (state.items.isSelectedSeasonWatched) {
                            Icon(
                                painter = painterResource(R.drawable.ic_check_double),
                                contentDescription = null,
                                tint = TraktTheme.colors.textPrimary,
                                modifier = Modifier
                                    .size(checkSize)
                                    .onClickCombined(
                                        enabled = !isLoading,
                                        onClick = {
                                            state.items.selectedSeason?.let {
                                                onRemoveSeasonClick?.invoke()
                                            }
                                        },
                                        onLongClick = {
                                            state.items.selectedSeason?.let {
                                                onRemoveSeasonClick?.invoke()
                                            }
                                        },
                                    ),
                            )
                        } else {
                            Icon(
                                painter = painterResource(R.drawable.ic_check),
                                contentDescription = null,
                                tint = TraktTheme.colors.accent,
                                modifier = Modifier
                                    .size(checkSize)
                                    .onClickCombined(
                                        enabled = !isLoading,
                                        onClick = {
                                            state.items.selectedSeason?.let {
                                                onCheckSeasonClick?.invoke()
                                            }
                                        },
                                        onLongClick = {
                                            state.items.selectedSeason?.let {
                                                onCheckSeasonClick?.invoke()
                                            }
                                        },
                                    ),
                            )
                        }
                    }
                }
            }
        }

        Crossfade(
            targetState = state.loading,
            animationSpec = tween(300),
        ) { loading ->
            when (loading) {
                IDLE, LOADING -> {
                    ContentLoading(
                        visible = loading.isLoading,
                        contentPadding = contentPadding,
                    )
                }
                DONE -> {
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
                            onCheckEpisodeClick = onCheckEpisodeClick,
                            onCheckEpisodeLongClick = onCheckEpisodeLongClick,
                            onRemoveEpisodeClick = onRemoveEpisodeClick,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ContentList(
    show: Show?,
    seasons: ShowSeasons,
    contentPadding: PaddingValues,
    onSeasonClick: ((Season) -> Unit)? = null,
    onEpisodeClick: ((EpisodeItem) -> Unit)? = null,
    onCheckEpisodeClick: ((EpisodeItem) -> Unit)? = null,
    onCheckEpisodeLongClick: ((EpisodeItem) -> Unit)? = null,
    onRemoveEpisodeClick: ((EpisodeItem) -> Unit)? = null,
) {
    Column(
        verticalArrangement = spacedBy(20.dp),
    ) {
        ShowSeasonsList(
            show = show,
            seasons = seasons.seasons,
            selectedSeason = seasons.selectedSeason?.number,
            onSeasonClick = onSeasonClick ?: {},
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
            onRemoveClick = onRemoveEpisodeClick ?: {},
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
            items(count = 6) {
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
                    loading = DONE,
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
                    loading = LOADING,
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
                    loading = DONE,
                ),
            )
        }
    }
}
