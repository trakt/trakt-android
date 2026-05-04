@file:OptIn(ExperimentalFoundationApi::class)

package tv.trakt.trakt.core.summary.shows.features.seasons.all

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.layout.LazyLayoutCacheWindow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType.Companion.Confirm
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight.Companion.W500
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import tv.trakt.trakt.LocalSnackbarState
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Idle
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.extensions.durationFormat
import tv.trakt.trakt.common.helpers.extensions.nowUtc
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.extensions.onClickCombined
import tv.trakt.trakt.common.helpers.extensions.relativeDateTimeString
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Images.Size
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.toTraktId
import tv.trakt.trakt.common.ui.composables.FilmProgressIndicator
import tv.trakt.trakt.core.summary.shows.features.seasons.model.EpisodeItem
import tv.trakt.trakt.core.summary.shows.features.seasons.model.SeasonItem
import tv.trakt.trakt.core.summary.shows.features.seasons.model.ShowSeasons
import tv.trakt.trakt.core.summary.shows.features.seasons.ui.ShowSeasonsList
import tv.trakt.trakt.helpers.SimpleScrollConnection
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.ScrollableBackdropImage
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.components.confirmation.ConfirmationSheet
import tv.trakt.trakt.ui.components.confirmation.RemoveConfirmationSheet
import tv.trakt.trakt.ui.components.dateselection.DateSelectionSheet
import tv.trakt.trakt.ui.components.mediacards.PanelHorizontalMediaCard
import tv.trakt.trakt.ui.components.mediacards.skeletons.PanelHorizontalMediaSkeletonCard
import tv.trakt.trakt.ui.components.mediacards.skeletons.VerticalMediaSkeletonCard
import tv.trakt.trakt.ui.theme.TraktTheme

private const val SEASON_ITEM_WIDTH_DP = 92
private const val SEASON_ITEM_SPACING_DP = 8

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AllShowSeasonsScreen(
    viewModel: AllShowSeasonsViewModel,
    onEpisodeClick: (showId: TraktId, episode: Episode) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val snack = LocalSnackbarState.current
    val scope = rememberCoroutineScope()

    val state by viewModel.state.collectAsStateWithLifecycle()

    var confirmRemoveEpisodeSheet by remember { mutableStateOf<EpisodeItem?>(null) }
    var confirmMarkSeasonSheet by remember { mutableStateOf(false) }
    var confirmRemoveSeasonSheet by remember { mutableStateOf(false) }
    var episodeDateSheet by remember { mutableStateOf<EpisodeItem?>(null) }
    var seasonDateSheet by remember { mutableStateOf(false) }

    AllShowSeasonsContent(
        state = state,
        modifier = modifier,
        onEpisodeClick = { viewModel.navigateToEpisode(it.episode) },
        onSeasonClick = viewModel::loadSeason,
        onCheckEpisodeClick = { viewModel.addToWatched(it.episode) },
        onCheckEpisodeLongClick = { episodeDateSheet = it },
        onRemoveEpisodeClick = { confirmRemoveEpisodeSheet = it },
        onCheckSeasonClick = { confirmMarkSeasonSheet = true },
        onRemoveSeasonClick = { confirmRemoveSeasonSheet = true },
        onBackClick = onNavigateBack,
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
        active = confirmRemoveSeasonSheet,
        onYes = {
            confirmRemoveSeasonSheet = false
            state.items.selectedSeason?.let { viewModel.removeFromWatched(it) }
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
        onDismiss = { episodeDateSheet = null },
    )

    DateSelectionSheet(
        active = seasonDateSheet,
        title = state.show?.title ?: "",
        onResult = { result ->
            viewModel.addToWatched(season = state.items, customDate = result)
        },
        onDismiss = { seasonDateSheet = false },
    )

    LaunchedEffect(state.info) {
        if (state.info == null) return@LaunchedEffect
        scope.launch {
            haptic.performHapticFeedback(Confirm)
            state.info?.get(context)?.let { snack.showSnackbar(it) }
        }
        viewModel.clearInfo()
    }

    LaunchedEffect(state.navigateEpisode) {
        state.navigateEpisode?.let {
            onEpisodeClick(it.first, it.second)
            viewModel.clearNavigation()
        }
    }
}

@Composable
private fun AllShowSeasonsContent(
    state: AllShowSeasonsState,
    modifier: Modifier = Modifier,
    onEpisodeClick: ((EpisodeItem) -> Unit)? = null,
    onSeasonClick: ((SeasonItem) -> Unit)? = null,
    onCheckEpisodeClick: ((EpisodeItem) -> Unit)? = null,
    onCheckEpisodeLongClick: ((EpisodeItem) -> Unit)? = null,
    onRemoveEpisodeClick: ((EpisodeItem) -> Unit)? = null,
    onCheckSeasonClick: (() -> Unit)? = null,
    onRemoveSeasonClick: (() -> Unit)? = null,
    onBackClick: (() -> Unit)? = null,
) {
    val listState = rememberLazyListState(
        cacheWindow = LazyLayoutCacheWindow(
            aheadFraction = 0.5F,
            behindFraction = 0.5F,
        ),
    )

    val listScrollConnection = rememberSaveable(saver = SimpleScrollConnection.Saver) {
        SimpleScrollConnection()
    }

    val horizontalPadding = TraktTheme.spacing.mainPageHorizontalSpace
    val contentPadding = PaddingValues(horizontal = horizontalPadding)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TraktTheme.colors.backgroundPrimary)
            .nestedScroll(listScrollConnection),
    ) {
        ScrollableBackdropImage(
            imageUrl = state.backgroundUrl,
            translation = listScrollConnection.resultOffset,
            imageAlpha = 0.1F,
        )

        LazyColumn(
            state = listState,
            overscrollEffect = null,
            contentPadding = PaddingValues(
                top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
                bottom = WindowInsets.navigationBars.asPaddingValues()
                    .calculateBottomPadding()
                    .plus(TraktTheme.size.navigationBarHeight)
                    .plus(32.dp),
            ),
        ) {
            item {
                TitleBar(
                    state = state,
                    title = state.items.selectedSeason?.number?.let {
                        when (it) {
                            0 -> stringResource(R.string.text_season_specials)
                            else -> stringResource(R.string.text_season_number, it)
                        }
                    },
                    subtitle = state.show?.title,
                    loading = state.loadingSeason.isLoading,
                    more = state.items.isSelectedSeasonReleased,
                    onSeasonClick = {
                        onSeasonClick?.invoke(it)
                    },
                    onBackClick = {
                        onBackClick?.invoke()
                    },
                    modifier = Modifier
                        .padding(contentPadding)
                        .padding(bottom = 8.dp),
                )
            }

            item {
                Crossfade(
                    targetState = state.loading,
                    animationSpec = tween(300),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                ) { loading ->
                    when (loading) {
                        Idle, Loading -> SeasonsSkeleton(
                            contentPadding = contentPadding,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Done -> ShowSeasonsList(
                            show = state.show,
                            seasons = state.items.seasons,
                            selectedSeason = state.items.selectedSeason?.number,
                            contentPadding = contentPadding,
                            overscrollEffect = null,
                            itemWidth = SEASON_ITEM_WIDTH_DP.dp,
                            itemSpacing = SEASON_ITEM_SPACING_DP.dp,
                            snapScrollEnabled = true,
                            onSeasonClick = onSeasonClick ?: {},
                            onSeasonLongClick = {
                                when {
                                    state.loadingSeason.isLoading -> return@ShowSeasonsList
                                    state.items.isSelectedSeasonWatched -> onRemoveSeasonClick?.invoke()
                                    else -> onCheckSeasonClick?.invoke()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }

            when {
                state.loading.isLoading || state.items.isSeasonLoading -> {
                    items(count = 10) {
                        PanelHorizontalMediaSkeletonCard(
                            modifier = Modifier
                                .padding(contentPadding)
                                .padding(bottom = 12.dp),
                        )
                    }
                }
                else -> {
                    items(
                        items = state.items.selectedSeasonEpisodes,
                        key = { it.episode.ids.trakt.value },
                    ) { item ->
                        EpisodeListItem(
                            show = state.show!!,
                            episode = item,
                            onClick = onEpisodeClick,
                            onCheckClick = onCheckEpisodeClick,
                            onCheckLongClick = onCheckEpisodeLongClick,
                            onRemoveClick = onRemoveEpisodeClick,
                            modifier = Modifier
                                .padding(contentPadding)
                                .padding(bottom = 12.dp)
                                .animateItem(fadeInSpec = null, fadeOutSpec = null),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TitleBar(
    state: AllShowSeasonsState,
    title: String?,
    subtitle: String?,
    loading: Boolean,
    more: Boolean,
    onSeasonClick: (SeasonItem) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
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

    Row(
        verticalAlignment = CenterVertically,
        horizontalArrangement = spacedBy(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(TraktTheme.size.titleBarHeight)
            .graphicsLayer { translationX = -2.dp.toPx() },
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_back_arrow),
            tint = TraktTheme.colors.textPrimary,
            contentDescription = null,
            modifier = Modifier.onClick { onBackClick() },
        )

        Row(
            horizontalArrangement = spacedBy(8.dp),
            verticalAlignment = CenterVertically,
            modifier = Modifier.onClick {
                seasonsMenuVisible.value = true
            },
        ) {
            TraktHeader(
                title = title ?: stringResource(R.string.list_title_seasons),
                subtitle = subtitle ?: "",
            )

            Box {
                Icon(
                    painter = painterResource(R.drawable.ic_chevron_all),
                    tint = TraktTheme.colors.textPrimary,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
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
                                onSeasonClick(season)
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EpisodeListItem(
    show: Show,
    episode: EpisodeItem,
    onClick: ((EpisodeItem) -> Unit)?,
    onCheckClick: ((EpisodeItem) -> Unit)?,
    onCheckLongClick: ((EpisodeItem) -> Unit)?,
    onRemoveClick: ((EpisodeItem) -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val isReleased = remember(episode.episode.firstAired) {
        val firstAired = episode.episode.firstAired
        firstAired != null && firstAired.isBefore(nowUtc())
    }

    PanelHorizontalMediaCard(
        title = episode.episode.title,
        subtitle = stringResource(
            R.string.episode_footer_season_episode,
            episode.episode.season,
            episode.episode.number,
        ),
        contentImageUrl = episode.episode.images?.getScreenshotUrl(Size.THUMB)
            ?: show.images?.getFanartUrl(Size.THUMB),
        containerImageUrl = episode.episode.images?.getScreenshotUrl(Size.THUMB),
        more = episode.isWatched && !episode.isLoading,
        watched = episode.isWatched,
        footerContent = {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (!isReleased) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        verticalAlignment = CenterVertically,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_calendar_upcoming),
                            contentDescription = null,
                            tint = TraktTheme.colors.textPrimary,
                            modifier = Modifier.size(13.dp),
                        )
                        Text(
                            text = episode.episode.firstAired?.relativeDateTimeString() ?: "TBA",
                            color = TraktTheme.colors.textPrimary,
                            style = TraktTheme.typography.cardSubtitle.copy(
                                fontSize = 11.sp,
                                fontWeight = W500,
                            ),
                        )
                    }
                } else {
                    val runtime = episode.episode.runtime?.inWholeMinutes
                    if (runtime != null) {
                        Text(
                            text = runtime.durationFormat(),
                            color = TraktTheme.colors.textPrimary,
                            style = TraktTheme.typography.cardSubtitle.copy(
                                fontSize = 11.sp,
                                fontWeight = W500,
                            ),
                        )
                    }
                }

                Box(
                    contentAlignment = Alignment.BottomCenter,
                    modifier = Modifier.size(20.dp),
                ) {
                    when {
                        episode.isLoading -> {
                            FilmProgressIndicator(size = 17.dp)
                        }
                        isReleased && !episode.isWatched && episode.isCheckable -> {
                            Icon(
                                painter = painterResource(R.drawable.ic_check),
                                contentDescription = null,
                                tint = TraktTheme.colors.accent,
                                modifier = Modifier
                                    .size(20.dp)
                                    .onClickCombined(
                                        onClick = { onCheckClick?.invoke(episode) },
                                        onLongClick = { onCheckLongClick?.invoke(episode) },
                                    ),
                            )
                        }
                    }
                }
            }
        },
        onClick = { onClick?.invoke(episode) },
        onLongClick = { if (episode.isWatched) onRemoveClick?.invoke(episode) },
        onImageClick = { onClick?.invoke(episode) },
        modifier = modifier,
    )

//    Row(
//        verticalAlignment = CenterVertically,
//        horizontalArrangement = spacedBy(12.dp),
//        modifier = modifier
//            .fillMaxWidth()
//            .padding(vertical = 6.dp)
//            .onClickCombined(
//                onClick = { onEpisodeClick?.invoke(item) },
//                onLongClick = {
//                    if (item.isWatched) onRemoveClick?.invoke(item)
//                },
//            ),
//    ) {
//        val imageUrl = item.episode.images?.getScreenshotUrl()
//            ?: show.images?.getFanartUrl()
//
//        Box(
//            modifier = Modifier
//                .width(120.dp)
//                .aspectRatio(HorizontalImageAspectRatio)
//                .clip(RoundedCornerShape(10.dp))
//                .background(TraktTheme.colors.placeholderContainer),
//        ) {
//            if (imageUrl != null) {
//                AsyncImage(
//                    model = ImageRequest.Builder(LocalContext.current)
//                        .data(imageUrl)
//                        .crossfade(true)
//                        .build(),
//                    contentDescription = item.episode.title,
//                    contentScale = ContentScale.Crop,
//                    modifier = Modifier.fillMaxSize(),
//                )
//            } else {
//                Icon(
//                    painter = painterResource(R.drawable.ic_placeholder_horizontal_border),
//                    contentDescription = null,
//                    tint = TraktTheme.colors.placeholderContent,
//                    modifier = Modifier
//                        .padding(4.dp)
//                        .align(Alignment.Center),
//                )
//            }
//        }
//
//        Column(
//            verticalArrangement = spacedBy(2.dp),
//            modifier = Modifier.weight(1f),
//        ) {
//            Text(
//                text = item.episode.title,
//                style = TraktTheme.typography.cardTitle,
//                color = TraktTheme.colors.textPrimary,
//                maxLines = 2,
//                overflow = TextOverflow.Ellipsis,
//            )
//            Text(
//                text = item.episode.seasonEpisode.toDisplayString(),
//                style = TraktTheme.typography.cardSubtitle,
//                color = TraktTheme.colors.textSecondary,
//                maxLines = 1,
//            )
//            if (!isReleased) {
//                Text(
//                    text = item.episode.firstAired?.relativeDateTimeString() ?: "TBA",
//                    style = TraktTheme.typography.cardSubtitle,
//                    color = TraktTheme.colors.accent,
//                    maxLines = 1,
//                )
//            }
//        }
//
//        Box(
//            contentAlignment = Alignment.Center,
//            modifier = Modifier.size(28.dp),
//        ) {
//            when {
//                item.isLoading -> {
//                    FilmProgressIndicator(size = 18.dp)
//                }
//                isReleased && !item.isWatched && item.isCheckable -> {
//                    Icon(
//                        painter = painterResource(R.drawable.ic_check),
//                        contentDescription = null,
//                        tint = TraktTheme.colors.accent,
//                        modifier = Modifier
//                            .size(20.dp)
//                            .onClickCombined(
//                                onClick = { onCheckClick?.invoke(item) },
//                                onLongClick = { onCheckLongClick?.invoke(item) },
//                            ),
//                    )
//                }
//                item.isWatched -> {
//                    Icon(
//                        painter = painterResource(R.drawable.ic_check_double),
//                        contentDescription = null,
//                        tint = TraktTheme.colors.accent,
//                        modifier = Modifier.size(20.dp),
//                    )
//                }
//            }
//        }
//    }
}

@Composable
private fun SeasonsSkeleton(
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        horizontalArrangement = spacedBy(SEASON_ITEM_SPACING_DP.dp),
        contentPadding = contentPadding,
        userScrollEnabled = false,
        overscrollEffect = null,
        modifier = modifier,
    ) {
        items(count = 5) {
            VerticalMediaSkeletonCard(
                width = SEASON_ITEM_WIDTH_DP.dp,
                secondaryChip = true,
            )
        }
    }
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun PreviewLoaded() {
    TraktTheme {
        val seasons = (1..5).map { n ->
            SeasonItem(
                season = PreviewData.season1.copy(
                    ids = PreviewData.season1.ids.copy(trakt = n.toTraktId()),
                    number = n,
                ),
                isWatched = n < 3,
            )
        }.toImmutableList()

        val episodes = (1..6).map { n ->
            EpisodeItem(
                episode = PreviewData.episode1.copy(
                    ids = PreviewData.episode1.ids.copy(trakt = n.toTraktId()),
                    number = n,
                    title = "Episode $n",
                ),
                isWatched = n < 4,
                isCheckable = true,
            )
        }.toImmutableList()

        AllShowSeasonsContent(
            state = AllShowSeasonsState(
                show = PreviewData.show1,
                user = PreviewData.user1,
                loading = Done,
                items = ShowSeasons(
                    seasons = seasons,
                    selectedSeason = PreviewData.season1.copy(number = 1),
                    selectedSeasonEpisodes = episodes,
                ),
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
private fun PreviewLoading() {
    TraktTheme {
        AllShowSeasonsContent(
            state = AllShowSeasonsState(
                loading = Loading,
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
private fun PreviewEmpty() {
    TraktTheme {
        AllShowSeasonsContent(
            state = AllShowSeasonsState(
                show = PreviewData.show1,
                loading = Done,
            ),
        )
    }
}
