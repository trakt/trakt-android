package tv.trakt.trakt.core.mixtape

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight.Companion.W400
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableSet
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.extensions.rememberDurationFormat
import tv.trakt.trakt.common.model.MediaMode
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.mixtape.MixtapeItem
import tv.trakt.trakt.core.mixtape.model.MixtapePicker
import tv.trakt.trakt.core.mixtape.ui.FlyingPoster
import tv.trakt.trakt.core.mixtape.ui.HideBottomBar
import tv.trakt.trakt.core.mixtape.ui.MixtapeDock
import tv.trakt.trakt.core.mixtape.ui.PosterFlight
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.InputField
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.components.chips.FilterChip
import tv.trakt.trakt.ui.components.chips.FilterChipGroup
import tv.trakt.trakt.ui.components.mediacards.VerticalMediaCard
import tv.trakt.trakt.ui.components.mediacards.skeletons.VerticalMediaSkeletonCard
import tv.trakt.trakt.ui.theme.TraktTheme

private const val SKELETON_COUNT = 9
private const val SETTLING_ALPHA = 0.5F
private const val FULL_ITEM_KEY = "full"
private const val VERTICAL_POSTER_RATIO = 2F / 3F

private val ROW_ITEM_SPACING = 12.dp
private val TypeOptions = persistentListOf(MediaMode.Shows, MediaMode.Movies)

@Composable
internal fun MixtapeScreen(
    viewModel: MixtapeViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToResults: (MediaType, List<MixtapeItem>) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val searchState = rememberTextFieldState()

    HideBottomBar()

    LaunchedEffect(searchState) {
        snapshotFlow { searchState.text.toString() }
            .collect(viewModel::setQuery)
    }

    MixtapeContent(
        state = state,
        searchState = searchState,
        onBackClick = onNavigateBack,
        onTypeClick = {
            searchState.clearText()
            viewModel.setType(it)
        },
        onPickClick = viewModel::pick,
        onRemoveClick = viewModel::removePick,
        onClearClick = viewModel::clearPicks,
        onMixClick = {
            viewModel.logMix()
            onNavigateToResults(state.type, state.picks)
        },
    )
}

@Composable
private fun MixtapeContent(
    state: MixtapeState,
    modifier: Modifier = Modifier,
    searchState: TextFieldState = rememberTextFieldState(),
    onBackClick: () -> Unit = {},
    onTypeClick: (MediaType) -> Unit = {},
    onPickClick: (MixtapeItem) -> Unit = {},
    onRemoveClick: (MixtapeItem) -> Unit = {},
    onClearClick: () -> Unit = {},
    onMixClick: () -> Unit = {},
) {
    val density = LocalDensity.current
    val gridState = rememberLazyGridState()
    val searchFocus = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    var dockHeight by remember { mutableStateOf(0.dp) }
    var rootOffset by remember { mutableStateOf(Offset.Zero) }
    var addSlotBounds by remember { mutableStateOf<Rect?>(null) }
    val flights = remember { mutableStateListOf<PosterFlight>() }
    val landingKeys = flights.map { it.item.key }.toImmutableSet()
    val departedKeys = (state.picks.map { it.key } + landingKeys).toImmutableSet()

    val contentPadding = PaddingValues(
        start = TraktTheme.spacing.mainPageHorizontalSpace,
        end = TraktTheme.spacing.mainPageHorizontalSpace,
        top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding().plus(4.dp),
        bottom = dockHeight.plus(TraktTheme.spacing.mainPageBottomSpace),
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TraktTheme.colors.backgroundPrimary)
            .onGloballyPositioned { rootOffset = it.positionInWindow() },
    ) {
        LazyVerticalGrid(
            state = gridState,
            columns = GridCells.Fixed(TraktTheme.size.mainGridColumns),
            horizontalArrangement = spacedBy(TraktTheme.spacing.mainGridHorizontalSpace),
            verticalArrangement = spacedBy(0.dp),
            contentPadding = contentPadding,
            overscrollEffect = null,
        ) {
            headerItems(
                state = state,
                searchState = searchState,
                searchFocus = searchFocus,
                onBackClick = onBackClick,
                onTypeClick = onTypeClick,
            )

            pickerItems(
                state = state,
                gridState = gridState,
                departedKeys = departedKeys,
                onPickClick = { item, posterBounds ->
                    focusManager.clearFocus()
                    val target = addSlotBounds
                    val canPick = !state.isFull && state.picks.none { it.id == item.id }
                    if (target != null && canPick) {
                        flights += PosterFlight(
                            item = item,
                            from = posterBounds.translate(-rootOffset),
                            to = target.translate(-rootOffset),
                        )
                    }
                    onPickClick(item)
                },
            )
        }

        MixtapeDock(
            picks = state.picks,
            landingKeys = landingKeys,
            onAddSlotPositioned = { addSlotBounds = it },
            onRemoveClick = onRemoveClick,
            onClearClick = onClearClick,
            onMixClick = onMixClick,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .onSizeChanged { dockHeight = with(density) { it.height.toDp() } },
        )

        flights.forEach { flight ->
            key(flight) {
                FlyingPoster(
                    flight = flight,
                    onLanded = { flights.remove(flight) },
                )
            }
        }
    }
}

private fun LazyGridScope.headerItems(
    state: MixtapeState,
    searchState: TextFieldState,
    searchFocus: FocusRequester,
    onBackClick: () -> Unit,
    onTypeClick: (MediaType) -> Unit,
) {
    item(span = { GridItemSpan(maxLineSpan) }) {
        Row(
            verticalAlignment = CenterVertically,
            horizontalArrangement = spacedBy(ROW_ITEM_SPACING),
            modifier = Modifier
                .fillMaxWidth()
                .height(TraktTheme.size.titleBarHeight),
        ) {
            Row(
                verticalAlignment = CenterVertically,
                horizontalArrangement = spacedBy(ROW_ITEM_SPACING),
                modifier = Modifier
                    .weight(1F)
                    .graphicsLayer { translationX = -2.dp.toPx() }
                    .onClick { onBackClick() },
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_back_arrow),
                    tint = TraktTheme.colors.textPrimary,
                    contentDescription = null,
                )
                TraktHeader(
                    title = stringResource(R.string.page_title_mixtape),
                )
            }

            TypeSelector(
                selected = state.type.toMediaMode(),
                onSelect = { onTypeClick(it.toMediaType()) },
            )
        }
    }

    item(span = { GridItemSpan(maxLineSpan) }) {
        Text(
            text = stringResource(
                when (state.type) {
                    MediaType.Show -> R.string.text_mixtape_description_shows
                    MediaType.Movie,
                    MediaType.Season,
                    MediaType.Episode,
                    -> R.string.text_mixtape_description_movies
                },
            ),
            style = TraktTheme.typography.paragraphSmall,
            color = TraktTheme.colors.textSecondary,
            modifier = Modifier
                .padding(
                    top = TraktTheme.spacing.mainGridVerticalSpace / 4,
                    bottom = 16.dp,
                ),
        )
    }

    item(span = { GridItemSpan(maxLineSpan) }) {
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .padding(bottom = TraktTheme.spacing.mainGridVerticalSpace)
                .graphicsLayer {
                    translationY = 2.dp.toPx()
                },
        ) {
            Text(
                text = stringResource(state.picker.kind.headerRes(state.type)),
                style = TraktTheme.typography.heading5,
                color = TraktTheme.colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1F),
            )

            if (state.picker.kind == MixtapePicker.Kind.Fits && !state.loading) {
                Text(
                    text = stringResource(
                        R.string.text_mixtape_fits_count,
                        state.picker.items.size.toString(),
                    ),
                    style = TraktTheme.typography.meta.copy(
                        fontWeight = W400,
                        lineHeight = 1.2.em,
                    ),
                    color = TraktTheme.colors.textSecondary,
                    modifier = Modifier
                        .padding(start = 16.dp),
                )
            }
        }
    }

    item(span = { GridItemSpan(maxLineSpan) }) {
        InputField(
            state = searchState,
            icon = painterResource(R.drawable.ic_search_off),
            placeholder = stringResource(
                when (state.type) {
                    MediaType.Show -> R.string.input_placeholder_mixtape_search_shows
                    MediaType.Movie,
                    MediaType.Season,
                    MediaType.Episode,
                    -> R.string.input_placeholder_mixtape_search_movies
                },
            ),
            endSlot = {
                if (searchState.text.isNotBlank()) {
                    Icon(
                        painter = painterResource(R.drawable.ic_close),
                        contentDescription = null,
                        tint = TraktTheme.colors.textSecondary,
                        modifier = Modifier
                            .size(18.dp)
                            .onClick { searchState.clearText() },
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = TraktTheme.spacing.mainGridVerticalSpace)
                .focusRequester(searchFocus),
        )
    }
}

private fun LazyGridScope.pickerItems(
    state: MixtapeState,
    gridState: LazyGridState,
    departedKeys: ImmutableSet<String>,
    onPickClick: (MixtapeItem, Rect) -> Unit,
) {
    when {
        state.isFull -> fullItem(gridState)

        state.loading -> items(count = SKELETON_COUNT) {
            VerticalMediaSkeletonCard(
                chipRatio = 0.66F,
                chipSpacing = 8.dp,
                modifier = Modifier
                    .padding(bottom = TraktTheme.spacing.mainGridVerticalSpace),
            )
        }

        state.picker.items.isEmpty() -> messageItem(
            textRes = when (state.picker.kind) {
                MixtapePicker.Kind.Search -> R.string.text_mixtape_no_search_results
                MixtapePicker.Kind.Fits -> R.string.text_mixtape_no_fits
                MixtapePicker.Kind.Recent -> R.string.text_mixtape_empty_history
            },
        )

        else -> items(
            count = state.picker.items.size,
            key = { index -> state.picker.items[index].item.key },
        ) { index ->
            val entry = state.picker.items[index]
            PickerCard(
                entry = entry,
                enabled = !state.settling,
                onClick = { posterBounds -> onPickClick(entry.item, posterBounds) },
                modifier = Modifier
                    .padding(bottom = TraktTheme.spacing.mainGridVerticalSpace)
                    .alpha(
                        when {
                            entry.item.key in departedKeys -> 0F
                            state.settling -> SETTLING_ALPHA
                            else -> 1F
                        },
                    )
                    .animateItem(),
            )
        }
    }
}

private fun LazyGridScope.messageItem(
    @StringRes textRes: Int,
) {
    item(span = { GridItemSpan(maxLineSpan) }) {
        Text(
            text = stringResource(textRes),
            style = TraktTheme.typography.paragraphSmall,
            color = TraktTheme.colors.textSecondary,
        )
    }
}

private fun LazyGridScope.fullItem(gridState: LazyGridState) {
    item(
        key = FULL_ITEM_KEY,
        span = { GridItemSpan(maxLineSpan) },
    ) {
        val density = LocalDensity.current
        val remainingHeight by remember(gridState) {
            derivedStateOf {
                val info = gridState.layoutInfo
                val item = info.visibleItemsInfo.firstOrNull { it.key == FULL_ITEM_KEY }
                    ?: return@derivedStateOf 0
                info.viewportEndOffset - info.afterContentPadding - item.offset.y
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = spacedBy(12.dp, CenterVertically),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = with(density) { remainingHeight.coerceAtLeast(0).toDp() }),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_mixtape),
                contentDescription = null,
                tint = TraktTheme.colors.textPrimary,
                modifier = Modifier.size(28.dp),
            )
            Text(
                text = stringResource(R.string.text_mixtape_max_picks),
                style = TraktTheme.typography.paragraphSmall,
                color = TraktTheme.colors.textPrimary,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun PickerCard(
    entry: MixtapePicker.Entry,
    enabled: Boolean,
    onClick: (posterBounds: Rect) -> Unit,
    modifier: Modifier = Modifier,
) {
    val coordinates = remember { mutableStateOf<LayoutCoordinates?>(null) }

    VerticalMediaCard(
        title = entry.item.title,
        imageUrl = entry.item.images?.getPosterUrl(),
        enabled = enabled && !entry.isOff,
        more = false,
        blackWhite = entry.isOff,
        chipSpacing = 10.dp,
        chipContent = { chipModifier ->
            Row(
                verticalAlignment = CenterVertically,
                horizontalArrangement = spacedBy(4.dp),
                modifier = chipModifier,
            ) {
                Icon(
                    painter = painterResource(entry.item.typeIcon()),
                    contentDescription = null,
                    tint = TraktTheme.colors.chipContent,
                    modifier = Modifier.size(13.dp),
                )
                Text(
                    text = when {
                        entry.isOff -> stringResource(R.string.tag_mixtape_different_mood)
                        else -> entry.item.subtitle()
                    },
                    style = TraktTheme.typography.cardTitle,
                    color = TraktTheme.colors.textPrimary,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        },
        onClick = {
            val card = coordinates.value ?: return@VerticalMediaCard
            val width = card.size.width.toFloat()
            onClick(Rect(card.positionInWindow(), Size(width, width / VERTICAL_POSTER_RATIO)))
        },
        modifier = modifier.onGloballyPositioned { coordinates.value = it },
    )
}

@Composable
private fun TypeSelector(
    selected: MediaMode,
    onSelect: (MediaMode) -> Unit,
) {
    FilterChipGroup(
        paddingVertical = PaddingValues.Zero,
    ) {
        for (mode in TypeOptions) {
            FilterChip(
                selected = selected == mode,
                height = 36.dp,
                text = stringResource(mode.displayRes),
                unselectedTextVisible = false,
                leadingContent = {
                    Icon(
                        painter = painterResource(mode.offIcon),
                        contentDescription = null,
                        tint = when (selected) {
                            mode -> TraktTheme.colors.textPrimaryOnAccent
                            else -> TraktTheme.colors.textPrimary
                        },
                        modifier = Modifier.size(16.dp),
                    )
                },
                onClick = { onSelect(mode) },
            )
        }
    }
}

@Composable
private fun MixtapeItem.subtitle(): String {
    return when (this) {
        is MixtapeItem.MovieItem -> {
            val year = movie.year?.toString() ?: "TBA"
            val runtime = rememberDurationFormat(movie.runtime?.inWholeMinutes)
            if (runtime != "N/A") "$year • $runtime" else year
        }
        is MixtapeItem.ShowItem -> {
            val year = show.year?.toString() ?: "TBA"
            val episodes = stringResource(R.string.tag_text_number_of_episodes, show.airedEpisodes)
            if (show.airedEpisodes > 0) "$year • $episodes" else year
        }
    }
}

private fun MixtapeItem.typeIcon(): Int {
    return when (this) {
        is MixtapeItem.MovieItem -> R.drawable.ic_movies_off
        is MixtapeItem.ShowItem -> R.drawable.ic_shows_off
    }
}

private fun MixtapePicker.Kind.headerRes(type: MediaType): Int {
    return when (this) {
        MixtapePicker.Kind.Search -> R.string.header_mixtape_search_results
        MixtapePicker.Kind.Fits -> R.string.header_mixtape_fits
        MixtapePicker.Kind.Recent -> when (type) {
            MediaType.Show -> R.string.header_mixtape_recent_shows
            MediaType.Movie,
            MediaType.Season,
            MediaType.Episode,
            -> R.string.header_mixtape_recent_movies
        }
    }
}

private fun MediaType.toMediaMode(): MediaMode {
    return when (this) {
        MediaType.Show -> MediaMode.Shows
        MediaType.Movie,
        MediaType.Season,
        MediaType.Episode,
        -> MediaMode.Movies
    }
}

private fun MediaMode.toMediaType(): MediaType {
    return when (this) {
        MediaMode.Shows,
        MediaMode.Media,
        -> MediaType.Show
        MediaMode.Movies -> MediaType.Movie
    }
}

@PreviewTraktLightDark
@Composable
private fun Preview() {
    TraktThemePreview {
        MixtapeContent(
            state = MixtapeState(loading = false),
        )
    }
}

@PreviewTraktLightDark
@Composable
private fun LoadingPreview() {
    TraktThemePreview {
        MixtapeContent(
            state = MixtapeState(),
        )
    }
}
