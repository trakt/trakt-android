@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package tv.trakt.trakt.core.userprofile.sections.lists.all

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.toImmutableList
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.model.CustomList
import tv.trakt.trakt.common.model.toTraktId
import tv.trakt.trakt.core.lists.sections.personal.model.PersonalListType
import tv.trakt.trakt.core.lists.sections.personal.model.PersonalListType.Collaborations
import tv.trakt.trakt.core.lists.sections.personal.model.PersonalListType.Personal
import tv.trakt.trakt.core.lists.sections.personal.ui.ListsFilters
import tv.trakt.trakt.helpers.SimpleScrollConnection
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.ScrollableBackdropImage
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.components.mediacards.CustomListCard
import tv.trakt.trakt.ui.components.mediacards.skeletons.CustomListSkeletonCard
import tv.trakt.trakt.ui.theme.HorizontalImageAspectRatio
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun AllUserProfileListsScreen(
    modifier: Modifier = Modifier,
    viewModel: AllUserProfileListsViewModel = koinViewModel(),
    onNavigateToList: (CustomList) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    AllUserProfileListsContent(
        state = state,
        modifier = modifier,
        onListClick = onNavigateToList,
        onFilterClick = viewModel::setFilter,
        onEndOfList = viewModel::loadMoreData,
        onBackClick = onNavigateBack,
    )
}

@Composable
private fun AllUserProfileListsContent(
    state: AllUserProfileListsState,
    modifier: Modifier = Modifier,
    onListClick: (CustomList) -> Unit = {},
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
                    subtitle = state.user.name,
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
                    options = listOf(Personal, Collaborations).toImmutableList(),
                    height = 32.dp,
                    paddingHorizontal = contentHorizontalPadding,
                    paddingVertical = PaddingValues(
                        top = 8.dp,
                        bottom = 20.dp,
                    ),
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
                            userVisible = true,
                            descriptionVisible = true,
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
                            userVisible = true,
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
                    Text(
                        text = stringResource(R.string.list_placeholder_empty),
                        color = TraktTheme.colors.textSecondary,
                        style = TraktTheme.typography.heading6,
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
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = CenterVertically,
        horizontalArrangement = spacedBy(12.dp),
        modifier = modifier
            .height(TraktTheme.size.titleBarHeight)
            .graphicsLayer {
                translationX = -2.dp.toPx()
            },
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_back_arrow),
            tint = TraktTheme.colors.textPrimary,
            contentDescription = null,
        )
        TraktHeader(
            title = stringResource(R.string.list_title_user_lists),
            subtitle = subtitle,
        )
    }
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview() {
    TraktTheme {
        AllUserProfileListsContent(
            state = AllUserProfileListsState(
                user = AllUserProfileListsState.User(0.toTraktId(), "Sean Doe"),
            ),
        )
    }
}
