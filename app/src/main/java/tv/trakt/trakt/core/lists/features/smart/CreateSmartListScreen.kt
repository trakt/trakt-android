@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.lists.features.smart

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SheetValue.Expanded
import androidx.compose.material3.SheetValue.Hidden
import androidx.compose.material3.SheetValue.PartiallyExpanded
import androidx.compose.material3.SnackbarDuration.Long
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tv.trakt.trakt.LocalBottomBarVisibility
import tv.trakt.trakt.LocalSnackbarState
import tv.trakt.trakt.common.helpers.extensions.DevicePreview
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.model.MediaMode.Movies
import tv.trakt.trakt.common.model.MediaMode.Shows
import tv.trakt.trakt.common.model.lists.SmartListFilters
import tv.trakt.trakt.common.model.lists.SmartListSource.Anticipated
import tv.trakt.trakt.common.model.lists.SmartListSource.Popular
import tv.trakt.trakt.common.model.lists.SmartListSource.Trending
import tv.trakt.trakt.common.model.sorting.Sorting
import tv.trakt.trakt.core.lists.features.details.ui.ListDetailsMovieView
import tv.trakt.trakt.core.lists.features.details.ui.ListDetailsShowView
import tv.trakt.trakt.core.lists.features.smart.ui.CreateSmartListSheetView
import tv.trakt.trakt.core.lists.model.SmartListItem
import tv.trakt.trakt.core.lists.model.SmartListItem.MovieItem
import tv.trakt.trakt.core.lists.model.SmartListItem.ShowItem
import tv.trakt.trakt.helpers.SimpleScrollConnection
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.ScrollableBackdropImage
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.components.confirmation.RemoveConfirmationSheet
import tv.trakt.trakt.ui.components.mediacards.skeletons.PanelMediaSkeletonCard
import tv.trakt.trakt.ui.theme.TraktTheme
import kotlin.time.Duration.Companion.milliseconds

private const val SHEET_PEEK_FRACTION = 0.15f
private const val SHEET_EXPANDED_FRACTION = 0.75f
private const val SHEET_SCRIM_ALPHA = 0.66f

@Composable
internal fun CreateSmartListScreen(
    viewModel: CreateSmartListViewModel,
    onNavigateBack: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val resources = LocalResources.current
    val snackbar = LocalSnackbarState.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.success) {
        if (state.success) {
            onNavigateBack()
        }
    }

    LaunchedEffect(state.error) {
        if (state.error != null) {
            snackbar.showSnackbar(
                message = state.error?.message ?: resources.getString(R.string.page_title_unexpected_error),
                duration = Long,
            )
            viewModel.clearError()
        }
    }

    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberBottomSheetState(
            initialValue = Hidden,
            enabledValues = setOf(Hidden, PartiallyExpanded, Expanded),
            confirmValueChange = { newValue -> newValue != Hidden },
        ),
    )

    val nameInputState = rememberTextFieldState()
    var confirmExitSheet by remember { mutableStateOf(false) }

    val onExit = {
        if (!state.creating.isLoading) {
            val hasName = nameInputState.text.isNotBlank()
            val hasChanges = state.filters != SmartListFilters.Default

            if (hasChanges || hasName) {
                confirmExitSheet = true
            } else {
                onNavigateBack()
            }
        }
    }

    CreateSmartListSheetContent(
        scope = scope,
        state = state,
        scaffoldState = scaffoldState,
        nameInputState = nameInputState,
        onCreateClick = { viewModel.createList(nameInputState.text.toString()) },
        onFiltersChange = viewModel::setFilters,
        onExit = onExit,
    )

    RemoveConfirmationSheet(
        active = confirmExitSheet,
        title = stringResource(R.string.dialog_title_discard_changes),
        message = stringResource(R.string.warning_prompt_discard_changes),
        onYes = {
            confirmExitSheet = false
            onNavigateBack()
        },
        onNo = { confirmExitSheet = false },
    )

    val localBottomBarVisibility = LocalBottomBarVisibility.current
    val hasLoaded = state.items != null

    LaunchedEffect(hasLoaded) {
        if (state.items != null) {
            delay(200.milliseconds)
            localBottomBarVisibility.value = false
            scaffoldState.bottomSheetState.expand()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            localBottomBarVisibility.value = true
        }
    }

    BackHandler {
        if (scaffoldState.bottomSheetState.currentValue == Expanded) {
            scope.launch { scaffoldState.bottomSheetState.partialExpand() }
        } else {
            onExit()
        }
    }
}

@Composable
private fun CreateSmartListSheetContent(
    scope: CoroutineScope,
    scaffoldState: BottomSheetScaffoldState,
    state: CreateSmartListState,
    nameInputState: TextFieldState,
    onCreateClick: (String) -> Unit,
    onFiltersChange: (filter: SmartListFilters) -> Unit,
    onExit: () -> Unit,
) {
    val screenHeight = LocalWindowInfo.current.containerDpSize.height
    val peekHeight = screenHeight * SHEET_PEEK_FRACTION
    val expandedHeight = screenHeight * SHEET_EXPANDED_FRACTION

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = peekHeight,
        sheetContainerColor = TraktTheme.colors.dialogContainer,
        sheetContentColor = TraktTheme.colors.dialogContent,
        containerColor = TraktTheme.colors.backgroundPrimary,
        sheetDragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 8.dp, bottom = 12.dp)
                    .background(
                        color = TraktTheme.colors.dialogContent,
                        shape = RoundedCornerShape(100),
                    )
                    .size(36.dp, 4.dp)
                    .clickable(
                        onClick = { },
                        indication = null,
                        interactionSource = null,
                    ),
            )
        },
        sheetContent = {
            CreateSmartListSheetView(
                state = state,
                nameState = nameInputState,
                onCreateClick = onCreateClick,
                onFiltersChange = onFiltersChange,
                modifier = Modifier
                    .heightIn(max = expandedHeight)
                    .padding(
                        start = 24.dp,
                        end = 24.dp,
                        bottom = WindowInsets.navigationBars.asPaddingValues()
                            .calculateBottomPadding()
                            .plus(24.dp),
                    ),
            )
        },
    ) {
        val scrimAlpha by animateFloatAsState(
            targetValue = if (scaffoldState.bottomSheetState.targetValue == Expanded) 1f else 0f,
            animationSpec = tween(durationMillis = 250),
        )

        Box(
            modifier = Modifier.fillMaxSize(),
        ) {
            CreateSmartListContent(
                state = state,
                onBack = onExit,
                bottomInset = peekHeight,
            )

            // Scrim: dims and blocks the preview while the sheet is expanded; tap collapses to peek.
            if (scrimAlpha > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(scrimAlpha)
                        .background(Color.Black.copy(alpha = SHEET_SCRIM_ALPHA))
                        .onClick {
                            scope.launch { scaffoldState.bottomSheetState.partialExpand() }
                        },
                )
            }
        }
    }
}

@Composable
private fun CreateSmartListContent(
    state: CreateSmartListState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    bottomInset: Dp = 0.dp,
) {
    val listState = rememberLazyListState()

    val listScrollConnection = rememberSaveable(saver = SimpleScrollConnection.Saver) {
        SimpleScrollConnection()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TraktTheme.colors.backgroundPrimary)
            .nestedScroll(listScrollConnection),
    ) {
        val contentPadding = PaddingValues(
            start = TraktTheme.spacing.mainPageHorizontalSpace,
            end = TraktTheme.spacing.mainPageHorizontalSpace,
            top = WindowInsets.statusBars.asPaddingValues()
                .calculateTopPadding(),
            bottom = WindowInsets.navigationBars.asPaddingValues()
                .calculateBottomPadding()
                .plus(TraktTheme.size.navigationBarHeight * 2)
                .plus(bottomInset),
        )

        ScrollableBackdropImage(
            translation = listScrollConnection.resultOffset,
        )

        LazyColumn(
            state = listState,
            verticalArrangement = spacedBy(0.dp),
            contentPadding = contentPadding,
            overscrollEffect = null,
            userScrollEnabled = !state.loading.isLoading,
            modifier = Modifier.fillMaxSize(),
        ) {
            item {
                TitleBar(
                    state = state,
                    onBackClick = onBack,
                    modifier = Modifier
                        .padding(bottom = 8.dp),
                )
            }

            val previewItems = state.items ?: EmptyImmutableList
            items(
                items = previewItems,
                key = SmartListItem::key,
            ) {
                when (it) {
                    is ShowItem -> ListDetailsShowView(
                        show = it.show,
                        showUserRating = null,
                        sorting = Sorting.Default,
                        showIcon = true,
                        more = false,
                        enabled = !state.loading.isLoading,
                        watched = state.collection.isWatched(it.id, it.type, it.show.airedEpisodes),
                        plays = state.collection.plays(it.id, it.type, it.show.airedEpisodes),
                        watching = state.collection.isWatching(it.id, it.type, it.show.airedEpisodes),
                        watchlist = state.collection.isWatchlist(it.id, it.type),
                        onClick = {},
                        onLongClick = {},
                        modifier = Modifier
                            .padding(bottom = TraktTheme.spacing.mainListVerticalSpace)
                            .animateItem(
                                fadeInSpec = null,
                                fadeOutSpec = null,
                            ),
                    )
                    is MovieItem -> ListDetailsMovieView(
                        movie = it.movie,
                        movieUserRating = null,
                        sorting = Sorting.Default,
                        showIcon = true,
                        more = false,
                        enabled = !state.loading.isLoading,
                        watched = state.collection.isWatched(it.id, it.type, null),
                        plays = state.collection.plays(it.id, it.type, null),
                        watchlist = state.collection.isWatchlist(it.id, it.type),
                        onClick = {},
                        onLongClick = {},
                        modifier = Modifier
                            .padding(bottom = TraktTheme.spacing.mainListVerticalSpace)
                            .animateItem(
                                fadeInSpec = null,
                                fadeOutSpec = null,
                            ),
                    )
                }
            }

            if (state.loading.isLoading && previewItems.isEmpty()) {
                items(10) {
                    PanelMediaSkeletonCard(
                        modifier = Modifier
                            .padding(bottom = TraktTheme.spacing.mainListVerticalSpace)
                            .animateItem(
                                fadeInSpec = null,
                                fadeOutSpec = null,
                            ),
                    )
                }
            }
        }
    }
}

@Composable
private fun TitleBar(
    state: CreateSmartListState,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = CenterVertically,
        horizontalArrangement = spacedBy(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(TraktTheme.size.titleBarHeight)
            .graphicsLayer {
                translationX = -2.dp.toPx()
            }
            .onClick {
                onBackClick()
            },
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_back_arrow),
            tint = TraktTheme.colors.textPrimary,
            contentDescription = null,
        )

        TraktHeader(
            title = when (state.filters.media) {
                Shows -> when (state.filters.source) {
                    Trending -> stringResource(R.string.list_title_trending_shows)
                    Popular -> stringResource(R.string.list_title_popular_shows)
                    Anticipated -> stringResource(R.string.list_title_anticipated_shows)
                    else -> stringResource(R.string.page_title_smart_lists)
                }
                Movies -> when (state.filters.source) {
                    Trending -> stringResource(R.string.list_title_trending_movies)
                    Popular -> stringResource(R.string.list_title_popular_movies)
                    Anticipated -> stringResource(R.string.list_title_anticipated_movies)
                    else -> stringResource(R.string.page_title_smart_lists)
                }
                else -> throw IllegalStateException("Unsupported media type: ${state.filters.media}")
            },
            subtitle = stringResource(R.string.tag_text_smart_list_preview),
        )
    }
}

@DevicePreview
@Composable
private fun CreateSmartListContentPreview() {
    TraktTheme {
        CreateSmartListContent(
            state = CreateSmartListState(),
            onBack = {},
        )
    }
}
