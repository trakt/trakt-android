@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package tv.trakt.trakt.core.lists.features.all

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.layout.LazyLayoutCacheWindow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
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
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.extensions.DevicePreview
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.model.CustomList
import tv.trakt.trakt.core.lists.sections.personal.model.PersonalListType
import tv.trakt.trakt.core.lists.sections.personal.model.PersonalListType.Collaborations
import tv.trakt.trakt.core.lists.sections.personal.model.PersonalListType.Liked
import tv.trakt.trakt.core.lists.sections.personal.model.PersonalListType.Personal
import tv.trakt.trakt.core.lists.sections.personal.ui.ListsFilters
import tv.trakt.trakt.core.lists.sheets.CreateListSheet
import tv.trakt.trakt.core.lists.sheets.EditListSheet
import tv.trakt.trakt.helpers.SimpleScrollConnection
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.ScrollableBackdropImage
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.components.mediacards.CustomListCard
import tv.trakt.trakt.ui.components.mediacards.skeletons.CustomListSkeletonCard
import tv.trakt.trakt.ui.theme.HorizontalImageAspectRatio
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun AllListsScreen(
    viewModel: AllListsViewModel,
    modifier: Modifier = Modifier,
    onNavigateList: (CustomList) -> Unit,
    onNavigatePersonalList: (CustomList) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    var createListSheet by remember { mutableStateOf(false) }
    var editListSheet by remember { mutableStateOf<CustomList?>(null) }

    AllListsScreen(
        state = state,
        modifier = modifier,
        onListClick = onNavigateList,
        onListPersonalClick = onNavigatePersonalList,
        onListCreateClick = { createListSheet = true },
        onListEditClick = { editListSheet = it },
        onFilterClick = viewModel::setFilter,
        onEndOfList = viewModel::loadMoreData,
        onBackClick = onNavigateBack,
    )

    CreateListSheet(
        active = createListSheet,
        onListCreated = {
            viewModel.loadData(reload = true)
        },
        onDismiss = { createListSheet = false },
    )

    EditListSheet(
        active = editListSheet != null,
        list = editListSheet,
        onDismiss = { editListSheet = null },
    )
}

@Composable
private fun AllListsScreen(
    state: AllListsState,
    modifier: Modifier = Modifier,
    onListClick: (CustomList) -> Unit = {},
    onListPersonalClick: (CustomList) -> Unit = {},
    onListCreateClick: () -> Unit = {},
    onListEditClick: (CustomList) -> Unit = {},
    onFilterClick: (PersonalListType) -> Unit = {},
    onEndOfList: () -> Unit = {},
    onBackClick: () -> Unit = {},
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

    val isScrolledToBottom by remember(state.items) {
        derivedStateOf {
            !state.items.isNullOrEmpty() &&
                listState.firstVisibleItemIndex >= (state.items.size - 3)
        }
    }

    LaunchedEffect(isScrolledToBottom) {
        if (isScrolledToBottom) {
            onEndOfList()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TraktTheme.colors.backgroundPrimary)
            .nestedScroll(listScrollConnection),
    ) {
        val contentVerticalPadding = PaddingValues(
            top = WindowInsets.statusBars.asPaddingValues()
                .calculateTopPadding(),
            bottom = WindowInsets.navigationBars.asPaddingValues()
                .calculateBottomPadding()
                .plus(TraktTheme.size.navigationBarHeight * 2),
        )

        val contentHorizontalPadding = PaddingValues(
            horizontal = TraktTheme.spacing.mainPageHorizontalSpace,
        )

        ScrollableBackdropImage(
            translation = listScrollConnection.resultOffset,
            imageUrl = state.user?.settings?.coverImage,
        )

        val listVisible = remember(state.items?.size, state.loading) {
            !state.items.isNullOrEmpty() && state.loading == Done
        }

        LazyColumn(
            state = listState,
            verticalArrangement = spacedBy(0.dp),
            contentPadding = contentVerticalPadding,
            overscrollEffect = null,
            userScrollEnabled = !state.loading.isLoading,
            modifier = modifier,
        ) {
            item {
                TitleBar(
                    createListVisible = state.filter == Personal,
                    onCreateListClick = onListCreateClick,
                    modifier = Modifier
                        .padding(contentHorizontalPadding)
                        .onClick(onClick = onBackClick)
                        .animateItem(
                            fadeInSpec = null,
                            fadeOutSpec = null,
                        ),
                )
            }

            item {
                ListsFilters(
                    height = 32.dp,
                    paddingHorizontal = contentHorizontalPadding,
                    paddingVertical = PaddingValues(bottom = 20.dp),
                    selected = state.filter,
                    onClick = onFilterClick,
                    modifier = Modifier.animateItem(
                        fadeInSpec = null,
                        fadeOutSpec = null,
                    ),
                )
            }

            itemsIndexed(
                items = state.items ?: EmptyImmutableList,
                key = { _, list -> list.ids.trakt.value },
            ) { index, list ->
                val verticalPadding = when (index) {
                    0 -> 0.dp
                    else -> 18.dp
                }
                when (state.filter) {
                    Personal if listVisible -> {
                        CustomListCard(
                            list = list,
                            descriptionVisible = true,
                            moreVisible = true,
                            onClick = { onListPersonalClick(list) },
                            onMoreClick = { onListEditClick(list) },
                            modifier = Modifier
                                .padding(contentHorizontalPadding)
                                .padding(top = verticalPadding)
                                .aspectRatio(HorizontalImageAspectRatio)
                                .animateItem(
                                    fadeInSpec = null,
                                    fadeOutSpec = null,
                                ),
                        )
                    }
                    Liked if listVisible -> {
                        CustomListCard(
                            list = list,
                            liked = true,
                            likesVisible = true,
                            onClick = { onListClick(list) },
                            modifier = Modifier
                                .padding(contentHorizontalPadding)
                                .padding(top = verticalPadding)
                                .aspectRatio(HorizontalImageAspectRatio)
                                .animateItem(
                                    fadeInSpec = null,
                                    fadeOutSpec = null,
                                ),
                        )
                    }
                    Collaborations if listVisible -> {
                        CustomListCard(
                            list = list,
                            onClick = { onListClick(list) },
                            modifier = Modifier
                                .padding(contentHorizontalPadding)
                                .padding(top = verticalPadding)
                                .aspectRatio(HorizontalImageAspectRatio)
                                .animateItem(
                                    fadeInSpec = null,
                                    fadeOutSpec = null,
                                ),
                        )
                    }
                    else -> {
                        // Noop
                    }
                }
            }

            items(
                count = when {
                    state.loading.isLoading -> 6
                    state.loadingMore.isLoading -> 1
                    else -> 0
                },
            ) { index ->
                CustomListSkeletonCard(
                    modifier = Modifier
                        .padding(contentHorizontalPadding)
                        .padding(
                            top = when (index) {
                                0 if state.items.isNullOrEmpty() -> 0.dp
                                else -> 18.dp
                            },
                        )
                        .aspectRatio(HorizontalImageAspectRatio)
                        .animateItem(
                            fadeInSpec = null,
                            fadeOutSpec = null,
                        ),
                )
            }

            if (state.items?.isEmpty() == true && state.loading == Done) {
                item {
                    ContentEmptyView(
                        modifier = Modifier
                            .padding(contentHorizontalPadding)
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
    createListVisible: Boolean,
    onCreateListClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxWidth()
            .height(TraktTheme.size.titleBarHeight)
            .graphicsLayer {
                translationX = -2.dp.toPx()
            },
    ) {
        Row(
            verticalAlignment = CenterVertically,
            horizontalArrangement = spacedBy(12.dp),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_back_arrow),
                tint = TraktTheme.colors.textPrimary,
                contentDescription = null,
            )
            TraktHeader(
                title = stringResource(R.string.list_title_personal_lists),
            )
        }

        AnimatedVisibility(
            visible = createListVisible,
            enter = fadeIn(tween(150)),
            exit = fadeOut(tween(150)),
            modifier = Modifier
                .graphicsLayer {
                    translationX = 1.dp.toPx()
                }
                .size(22.dp)
                .onClick(onClick = onCreateListClick),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_plus),
                contentDescription = null,
                tint = TraktTheme.colors.textPrimary,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun ContentEmptyView(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.list_placeholder_empty),
        color = TraktTheme.colors.textSecondary,
        style = TraktTheme.typography.heading6,
        modifier = modifier,
    )
}

@DevicePreview
@Composable
internal fun AllListsScreenPreview() {
    TraktTheme {
        AllListsScreen(
            state = AllListsState(),
        )
    }
}
