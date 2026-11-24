@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package tv.trakt.trakt.core.lists.sections.smart.details

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.layout.LazyLayoutCacheWindow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.ColorImage
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.LocalSnackbarState
import tv.trakt.trakt.common.core.user.UserCollectionState
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.model.Ids
import tv.trakt.trakt.common.model.MediaGenre
import tv.trakt.trakt.common.model.MediaMode
import tv.trakt.trakt.common.model.SlugId
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.common.model.lists.CustomList
import tv.trakt.trakt.common.model.lists.SmartList
import tv.trakt.trakt.common.model.lists.SmartListFilters
import tv.trakt.trakt.common.model.lists.SmartListRecipePreview
import tv.trakt.trakt.common.model.lists.SmartListSource
import tv.trakt.trakt.common.model.sorting.Sorting
import tv.trakt.trakt.core.lists.features.details.ui.ListDetailsMovieView
import tv.trakt.trakt.core.lists.features.details.ui.ListDetailsShowView
import tv.trakt.trakt.core.lists.model.SmartListItem
import tv.trakt.trakt.core.lists.model.SmartListItem.MovieItem
import tv.trakt.trakt.core.lists.model.SmartListItem.ShowItem
import tv.trakt.trakt.core.lists.sections.smart.details.recipe.SmartListRecipeSheet
import tv.trakt.trakt.core.movies.ui.context.sheet.MovieContextSheet
import tv.trakt.trakt.core.shows.ui.context.sheet.ShowContextSheet
import tv.trakt.trakt.helpers.SimpleScrollConnection
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.ScrollableBackdropImage
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.components.confirmation.RemoveConfirmationSheet
import tv.trakt.trakt.ui.components.mediacards.list.SmartListDropdown
import tv.trakt.trakt.ui.components.mediacards.skeletons.PanelMediaSkeletonCard
import tv.trakt.trakt.ui.theme.TraktTheme
import java.time.ZonedDateTime

@Composable
internal fun SmartListDetailsScreen(
    modifier: Modifier = Modifier,
    viewModel: SmartListDetailsViewModel,
    onShowClick: (TraktId) -> Unit,
    onMovieClick: (TraktId) -> Unit,
    onListClick: (CustomList) -> Unit,
    onEditClick: (SmartList) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val snack = LocalSnackbarState.current

    LaunchedEffect(state.error) {
        if (state.error != null) {
            snack.showSnackbar(
                message = state.error?.localizedMessage ?: "",
                duration = SnackbarDuration.Long,
            )
            viewModel.clearError()
        }
    }

    LaunchedEffect(state.deleted) {
        if (state.deleted) {
            onNavigateBack()
        }
    }

    LaunchedEffect(state.navigateShow, state.navigateMovie) {
        state.navigateShow?.let {
            onShowClick(it)
            viewModel.clearNavigation()
        }
        state.navigateMovie?.let {
            onMovieClick(it)
            viewModel.clearNavigation()
        }
    }

    var showContextSheet by remember { mutableStateOf<ShowItem?>(null) }
    var movieContextSheet by remember { mutableStateOf<MovieItem?>(null) }
    var confirmDeleteSheet by remember { mutableStateOf(false) }
    var recipeSheet by remember { mutableStateOf(false) }

    SmartListDetailsContent(
        state = state,
        modifier = modifier,
        onLoadMoreData = viewModel::loadMoreData,
        onClick = {
            when (it) {
                is MovieItem -> viewModel.navigateToMovie(it.movie)
                is ShowItem -> viewModel.navigateToShow(it.show)
            }
        },
        onLongClick = {
            when (it) {
                is MovieItem -> movieContextSheet = it
                is ShowItem -> showContextSheet = it
            }
        },
        onEditClick = {
            state.list?.let(onEditClick)
        },
        onDeleteClick = {
            confirmDeleteSheet = true
        },
        onRecipeClick = {
            recipeSheet = true
        },
        onBackClick = onNavigateBack,
    )

    SmartListRecipeSheet(
        list = state.list.takeIf { recipeSheet },
        onDismiss = {
            recipeSheet = false
        },
    )

    ShowContextSheet(
        onListClick = onListClick,
        show = showContextSheet?.show,
        onDismiss = {
            showContextSheet = null
        },
    )

    MovieContextSheet(
        onListClick = onListClick,
        movie = movieContextSheet?.movie,
        onDismiss = {
            movieContextSheet = null
        },
    )

    RemoveConfirmationSheet(
        active = confirmDeleteSheet,
        title = stringResource(R.string.confirmation_title_delete_list),
        message = stringResource(
            R.string.warning_prompt_delete_list,
            state.list?.name.orEmpty(),
        ),
        onYes = {
            viewModel.deleteList()
            confirmDeleteSheet = false
        },
        onNo = { confirmDeleteSheet = false },
    )
}

@Composable
internal fun SmartListDetailsContent(
    state: SmartListDetailsState,
    modifier: Modifier = Modifier,
    onClick: (SmartListItem) -> Unit = {},
    onLongClick: (SmartListItem) -> Unit = {},
    onEditClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
    onRecipeClick: () -> Unit = {},
    onBackClick: () -> Unit = {},
    onLoadMoreData: () -> Unit = {},
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
                .plus(TraktTheme.size.navigationBarHeight * 2),
        )

        ScrollableBackdropImage(
            translation = listScrollConnection.resultOffset,
        )

        ContentList(
            title = state.list?.name ?: "",
            subtitle = state.list?.rememberDescription(),
            recipePreview = state.list?.rememberRecipePreview(),
            listItems = (state.items ?: emptyList()).toImmutableList(),
            listState = listState,
            collectionState = state.collection,
            contentPadding = contentPadding,
            loading = state.loading.isLoading || state.deleting.isLoading,
            loadingMore = state.loadingMore.isLoading,
            onEndOfList = onLoadMoreData,
            onClick = onClick,
            onLongClick = onLongClick,
            onEditClick = onEditClick,
            onDeleteClick = onDeleteClick,
            onRecipeClick = onRecipeClick,
            onBackClick = onBackClick,
        )
    }
}

@Composable
private fun TitleBar(
    title: String,
    subtitle: String?,
    subtitleVisible: Boolean,
    modifier: Modifier = Modifier,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onRecipeClick: () -> Unit,
    onBackClick: () -> Unit,
) {
    Row(
        verticalAlignment = CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxWidth()
            .height(TraktTheme.size.titleBarHeight),
    ) {
        Row(
            verticalAlignment = CenterVertically,
            horizontalArrangement = spacedBy(12.dp),
            modifier = Modifier
                .weight(1F, fill = false)
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
                title = title,
                subtitle = when {
                    subtitleVisible -> subtitle
                    else -> null
                },
                modifier = Modifier
                    .weight(1F, fill = false),
            )
        }

        SmartListDropdown(
            onEditClick = onEditClick,
            onDeleteClick = onDeleteClick,
            onRecipeClick = onRecipeClick,
            modifier = Modifier
                .padding(start = 12.dp),
        )
    }
}

@Composable
private fun ContentList(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues,
    title: String,
    subtitle: String?,
    recipePreview: SmartListRecipePreview?,
    listState: LazyListState,
    listItems: ImmutableList<SmartListItem>,
    collectionState: UserCollectionState,
    loading: Boolean,
    loadingMore: Boolean,
    onClick: (SmartListItem) -> Unit,
    onLongClick: (SmartListItem) -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onRecipeClick: () -> Unit,
    onBackClick: () -> Unit,
    onEndOfList: () -> Unit,
) {
    val isScrolledToBottom by remember(listItems.size) {
        derivedStateOf {
            listState.firstVisibleItemIndex >= (listItems.size - 5)
        }
    }

    LaunchedEffect(isScrolledToBottom) {
        if (isScrolledToBottom) {
            onEndOfList()
        }
    }

    LazyColumn(
        state = listState,
        verticalArrangement = spacedBy(0.dp),
        contentPadding = contentPadding,
        overscrollEffect = null,
        modifier = modifier,
    ) {
        item {
            TitleBar(
                title = title,
                subtitle = subtitle,
                subtitleVisible = false,
                onEditClick = onEditClick,
                onDeleteClick = onDeleteClick,
                onRecipeClick = onRecipeClick,
                onBackClick = onBackClick,
            )
        }

        item {
            RecipeLink(
                preview = recipePreview,
                onClick = onRecipeClick,
            )
        }

        items(
            items = listItems,
            key = { item -> item.key },
        ) { item ->
            when (item) {
                is ShowItem -> ListDetailsShowView(
                    show = item.show,
                    showUserRating = null,
                    enabled = !loading,
                    watched = collectionState.isWatched(item.id, item.type, item.show.airedEpisodes),
                    watching = collectionState.isWatching(item.id, item.type, item.show.airedEpisodes),
                    watchlist = collectionState.isWatchlist(item.id, item.type),
                    showIcon = true,
                    sorting = Sorting.Default,
                    onClick = { onClick(item) },
                    onLongClick = { onLongClick(item) },
                    modifier = Modifier
                        .padding(bottom = TraktTheme.spacing.mainListVerticalSpace)
                        .animateItem(
                            fadeInSpec = null,
                            fadeOutSpec = null,
                        ),
                )

                is MovieItem -> ListDetailsMovieView(
                    movie = item.movie,
                    movieUserRating = null,
                    enabled = !loading,
                    watched = collectionState.isWatched(item.id, item.type, null),
                    watchlist = collectionState.isWatchlist(item.id, item.type),
                    showIcon = true,
                    sorting = Sorting.Default,
                    onClick = { onClick(item) },
                    onLongClick = { onLongClick(item) },
                    modifier = Modifier
                        .padding(bottom = TraktTheme.spacing.mainListVerticalSpace)
                        .animateItem(
                            fadeInSpec = null,
                            fadeOutSpec = null,
                        ),
                )
            }
        }

        if (loading && listItems.isEmpty()) {
            items(5) {
                PanelMediaSkeletonCard(
                    modifier = Modifier
                        .padding(bottom = TraktTheme.spacing.mainListVerticalSpace)
                        .animateItem(
                            fadeInSpec = null,
                            fadeOutSpec = null,
                        ),
                )
            }
        } else if (loadingMore && listItems.isNotEmpty()) {
            items(1) {
                PanelMediaSkeletonCard(
                    modifier = Modifier
                        .padding(bottom = TraktTheme.spacing.mainListVerticalSpace)
                        .animateItem(
                            fadeInSpec = null,
                            fadeOutSpec = null,
                        ),
                )
            }
        } else if (listItems.isEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.list_placeholder_empty),
                    color = TraktTheme.colors.textSecondary,
                    style = TraktTheme.typography.heading6,
                )
            }
        }
    }
}

@Composable
private fun RecipeLink(
    preview: SmartListRecipePreview?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = CenterVertically,
        horizontalArrangement = spacedBy(4.dp),
        modifier = modifier
            .fillMaxWidth()
            .onClick(enabled = preview != null, onClick = onClick)
            .padding(bottom = 22.dp),
    ) {
        val fontStyle = TraktTheme.typography.meta.copy(
            fontSize = 12.sp,
        )
        Text(
            text = preview?.summary.orEmpty(),
            color = TraktTheme.colors.textSecondary,
            style = fontStyle,
            maxLines = 1,
            overflow = Ellipsis,
            modifier = Modifier.weight(1F),
        )

        if (preview != null && preview.remaining > 0) {
            Text(
                text = stringResource(R.string.text_smart_list_recipe_more, preview.remaining),
                color = TraktTheme.colors.textSecondary,
                style = fontStyle,
                maxLines = 1,
                modifier = Modifier.padding(start = 16.dp),
            )
        }

        if (preview != null) {
            Icon(
                painter = painterResource(R.drawable.ic_chevron_right),
                contentDescription = null,
                tint = TraktTheme.colors.textSecondary,
                modifier = Modifier.size(14.dp),
            )
        }
    }
}

@OptIn(ExperimentalCoilApi::class)
@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
    locale = "en",
)
@Composable
private fun Preview() {
    TraktTheme {
        val previewHandler = AsyncImagePreviewHandler {
            ColorImage(Color.Blue.toArgb())
        }
        CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
            SmartListDetailsContent(
                state = SmartListDetailsState(
                    loading = LoadingState.Loading,
                    list = SmartList(
                        ids = Ids(trakt = TraktId(1), slug = SlugId("smart")),
                        name = "Unwatched sci-fi from the 2010s",
                        privacy = null,
                        createdAt = ZonedDateTime.now(),
                        updatedAt = ZonedDateTime.now(),
                        images = null,
                        filters = SmartListFilters(
                            source = SmartListSource.Popular,
                            media = MediaMode.Movies,
                            genres = persistentListOf(MediaGenre.ScienceFiction, MediaGenre.Thriller),
                            genresMatchAll = true,
                            excludedGenres = persistentListOf(MediaGenre.Horror),
                            keywords = persistentListOf("time-travel", "-zombie"),
                            countries = persistentListOf("gb", "fr"),
                            availability = persistentListOf(GlobalFilter.Availability.StreamingNow),
                            watchnowCountry = "us",
                            years = persistentListOf(2010, 2019),
                            ratings = persistentListOf(70, 100),
                            runtimes = persistentListOf(90, 150),
                            ignoreWatched = true,
                            ignoreWatchlisted = true,
                        ),
                    ),
                ),
            )
        }
    }
}

@PreviewTraktLightDark
@Composable
private fun RecipeLinkPreview() {
    TraktThemePreview {
        Column {
            RecipeLink(
                preview = SmartListRecipePreview(
                    summary = "Popular, Science Fiction, Thriller",
                    remaining = 6,
                ),
                onClick = {},
            )
            RecipeLink(
                preview = SmartListRecipePreview(
                    summary = "Trending, Comedy",
                    remaining = 0,
                ),
                onClick = {},
            )
            RecipeLink(
                preview = SmartListRecipePreview(
                    summary = "Anticipated, Science Fiction, Fantasy, Adventure, Animation, Documentary",
                    remaining = 12,
                ),
                onClick = {},
            )
        }
    }
}
