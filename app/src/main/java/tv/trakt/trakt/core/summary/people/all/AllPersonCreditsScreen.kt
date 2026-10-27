@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.summary.people.all

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.persistentListOf
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.MediaMode
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.movies.ui.context.sheet.MovieContextSheet
import tv.trakt.trakt.core.shows.ui.context.sheet.ShowContextSheet
import tv.trakt.trakt.core.summary.people.model.PersonCreditItem
import tv.trakt.trakt.core.summary.people.model.PersonCreditsSection
import tv.trakt.trakt.core.summary.people.ui.CreditsPositionFilters
import tv.trakt.trakt.core.summary.people.ui.PersonCreditCard
import tv.trakt.trakt.helpers.SimpleScrollConnection
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.MediaModeFilters
import tv.trakt.trakt.ui.components.ScrollableBackdropImage
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.components.mediacards.skeletons.VerticalMediaSkeletonCard
import tv.trakt.trakt.ui.theme.TraktTheme

private const val SKELETON_ITEMS = 12

@Composable
internal fun AllPersonCreditsScreen(
    modifier: Modifier = Modifier,
    viewModel: AllPersonCreditsViewModel = koinViewModel(),
    onNavigateToShow: (TraktId) -> Unit,
    onNavigateToMovie: (TraktId) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    var contextShowSheet by remember { mutableStateOf<Show?>(null) }
    var contextMovieSheet by remember { mutableStateOf<Movie?>(null) }

    LaunchedEffect(
        state.navigateShow,
        state.navigateMovie,
    ) {
        state.navigateShow?.let {
            viewModel.clearNavigation()
            onNavigateToShow(it)
        }
        state.navigateMovie?.let {
            viewModel.clearNavigation()
            onNavigateToMovie(it)
        }
    }

    AllPersonCreditsContent(
        state = state,
        modifier = modifier,
        onBackClick = onNavigateBack,
        onModeClick = viewModel::setMode,
        onPositionClick = viewModel::setPosition,
        onShowClick = viewModel::navigateToShow,
        onMovieClick = viewModel::navigateToMovie,
        onShowLongClick = { contextShowSheet = it },
        onMovieLongClick = { contextMovieSheet = it },
    )

    ShowContextSheet(
        show = contextShowSheet,
        onDismiss = { contextShowSheet = null },
    )

    MovieContextSheet(
        movie = contextMovieSheet,
        onDismiss = { contextMovieSheet = null },
    )
}

@Composable
internal fun AllPersonCreditsContent(
    state: AllPersonCreditsState,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onModeClick: (MediaMode) -> Unit = {},
    onPositionClick: (String) -> Unit = {},
    onShowClick: (Show) -> Unit = {},
    onMovieClick: (Movie) -> Unit = {},
    onShowLongClick: (Show) -> Unit = {},
    onMovieLongClick: (Movie) -> Unit = {},
) {
    val gridState = rememberLazyGridState()

    val scrollConnection = rememberSaveable(saver = SimpleScrollConnection.Saver) {
        SimpleScrollConnection()
    }

    val contentPadding = PaddingValues(
        start = TraktTheme.spacing.mainPageHorizontalSpace,
        end = TraktTheme.spacing.mainPageHorizontalSpace,
        top = WindowInsets.statusBars.asPaddingValues()
            .calculateTopPadding()
            .plus(3.dp),
        bottom = WindowInsets.navigationBars.asPaddingValues()
            .calculateBottomPadding()
            .plus(TraktTheme.size.navigationBarHeight * 2),
    )

    LaunchedEffect(state.mode, state.selectedPosition) {
        gridState.scrollToItem(0)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TraktTheme.colors.backgroundPrimary)
            .nestedScroll(scrollConnection),
    ) {
        ScrollableBackdropImage(
            translation = scrollConnection.resultOffset,
        )

        LazyVerticalGrid(
            state = gridState,
            columns = GridCells.Fixed(TraktTheme.size.mainGridColumns),
            horizontalArrangement = spacedBy(TraktTheme.spacing.mainGridHorizontalSpace),
            verticalArrangement = spacedBy(TraktTheme.spacing.mainGridVerticalSpace),
            contentPadding = contentPadding,
            overscrollEffect = null,
        ) {
            headerContent(
                state = state,
                onBackClick = onBackClick,
                onModeClick = onModeClick,
                onPositionClick = onPositionClick,
            )

            when {
                state.loading.isLoading && state.items.isEmpty() -> {
                    items(count = SKELETON_ITEMS) {
                        VerticalMediaSkeletonCard(
                            chip = true,
                            secondaryChip = true,
                            chipRatio = 0.66F,
                            secondaryChipRatio = 0.5F,
                            chipSpacing = 8.dp,
                        )
                    }
                }

                state.loading.isDone && state.items.isEmpty() && state.error != null -> {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        ContentErrorView(error = state.error)
                    }
                }

                state.loading.isDone && state.items.isEmpty() -> {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        ContentEmptyView(state = state)
                    }
                }

                else -> {
                    items(
                        items = state.items,
                        key = { it.key },
                    ) { item ->
                        PersonCreditCard(
                            item = item,
                            userCollection = state.collection,
                            onShowClick = onShowClick,
                            onShowLongClick = onShowLongClick,
                            onMovieClick = onMovieClick,
                            onMovieLongClick = onMovieLongClick,
                            modifier = Modifier.animateItem(
                                fadeInSpec = null,
                                fadeOutSpec = null,
                            ),
                        )
                    }
                }
            }
        }
    }
}

private fun LazyGridScope.headerContent(
    state: AllPersonCreditsState,
    onBackClick: () -> Unit,
    onModeClick: (MediaMode) -> Unit,
    onPositionClick: (String) -> Unit,
) {
    item(span = { GridItemSpan(maxLineSpan) }) {
        TitleBar(
            title = state.person?.name ?: "",
            subtitle = subtitleRes(state)?.let { stringResource(it) },
            modifier = Modifier.onClick { onBackClick() },
        )
    }

    item(span = { GridItemSpan(maxLineSpan) }) {
        MediaModeFilters(
            selected = state.mode,
            height = 32.dp,
            onClick = onModeClick,
        )
    }

    if (state.positions.size > 1) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            CreditsPositionFilters(
                filters = state.positions,
                selected = state.selectedPosition,
                paddingHorizontal = PaddingValues(),
                paddingVertical = PaddingValues(),
                onSelected = onPositionClick,
            )
        }
    }
}

@Composable
private fun TitleBar(
    title: String,
    subtitle: String?,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = CenterVertically,
        horizontalArrangement = spacedBy(12.dp),
        modifier = modifier
            .height(TraktTheme.size.titleBarHeight)
            .graphicsLayer {
                translationX = -4.dp.toPx()
            },
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_back_arrow),
            tint = TraktTheme.colors.textPrimary,
            contentDescription = null,
        )
        TraktHeader(
            title = title,
            subtitle = subtitle,
        )
    }
}

@Composable
private fun ContentEmptyView(state: AllPersonCreditsState) {
    Text(
        text = when {
            state.section.isHistory -> stringResource(
                R.string.list_placeholder_from_my_history,
                state.person?.name ?: "",
            )
            else -> stringResource(R.string.list_placeholder_empty)
        },
        color = TraktTheme.colors.textSecondary,
        style = TraktTheme.typography.heading6,
    )
}

@Composable
private fun ContentErrorView(error: Exception) {
    Text(
        text = "${stringResource(R.string.error_text_unexpected_error_short)}\n\n$error",
        color = TraktTheme.colors.textSecondary,
        style = TraktTheme.typography.meta,
        maxLines = 10,
    )
}

@StringRes
private fun subtitleRes(state: AllPersonCreditsState): Int? {
    if (state.section.isHistory) return R.string.list_title_from_my_history

    return when (state.mode) {
        MediaMode.Shows -> R.string.list_title_show_credits
        MediaMode.Movies -> R.string.list_title_movie_credits
        MediaMode.Media -> null
    }
}

@PreviewTraktLightDark
@Composable
private fun Preview() {
    TraktThemePreview {
        AllPersonCreditsContent(
            state = AllPersonCreditsState(
                section = PersonCreditsSection.Movies,
                mode = MediaMode.Movies,
                person = PreviewData.person1,
                positions = persistentListOf("acting", "directing"),
                selectedPosition = "acting",
                items = persistentListOf(
                    PersonCreditItem.MovieItem(
                        movie = PreviewData.movie1,
                        credit = "Neo",
                    ),
                ),
                loading = LoadingState.Done,
            ),
        )
    }
}

@PreviewTraktLightDark
@Composable
private fun HistoryEmptyPreview() {
    TraktThemePreview {
        AllPersonCreditsContent(
            state = AllPersonCreditsState(
                section = PersonCreditsSection.History,
                person = PreviewData.person1,
                loading = LoadingState.Done,
            ),
        )
    }
}
