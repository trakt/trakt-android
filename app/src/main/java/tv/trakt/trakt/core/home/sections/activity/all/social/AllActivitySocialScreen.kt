@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package tv.trakt.trakt.core.home.sections.activity.all.social

import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.core.home.sections.activity.all.AllActivityState
import tv.trakt.trakt.core.home.sections.activity.all.views.AllActivityEpisodeItem
import tv.trakt.trakt.core.home.sections.activity.all.views.AllActivityMovieItem
import tv.trakt.trakt.core.home.sections.activity.all.views.UserFilterChip
import tv.trakt.trakt.core.home.sections.activity.model.HomeActivityItem
import tv.trakt.trakt.core.home.sections.activity.model.HomeActivityItem.EpisodeItem
import tv.trakt.trakt.core.home.sections.activity.model.HomeActivityItem.MovieItem
import tv.trakt.trakt.core.home.sections.upnext.features.all.AllHomeUpNextContent
import tv.trakt.trakt.core.home.sections.upnext.features.all.AllHomeUpNextState
import tv.trakt.trakt.helpers.rememberHeaderState
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.FilterChipGroup
import tv.trakt.trakt.ui.components.ScrollableBackdropImage
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun AllActivitySocialScreen(
    modifier: Modifier = Modifier,
    viewModel: AllActivitySocialViewModel = koinViewModel(),
    onNavigateBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    AllActivitySocialContent(
        state = state,
        modifier = modifier,
        onFilterClick = { user ->
            viewModel.setUserFilter(user)
        },
        onBackClick = onNavigateBack,
        onLoadMore = {
            // No pagination at the moment.
        },
    )
}

@Composable
private fun TitleBar(modifier: Modifier = Modifier) {
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
        Text(
            text = stringResource(R.string.list_title_social_activity),
            color = TraktTheme.colors.textPrimary,
            style = TraktTheme.typography.heading5,
        )
    }
}

@Composable
internal fun AllActivitySocialContent(
    state: AllActivityState,
    modifier: Modifier = Modifier,
    onFilterClick: (User) -> Unit = {},
    onBackClick: () -> Unit = {},
    onLoadMore: () -> Unit = {},
) {
    val headerState = rememberHeaderState()
    val listState = rememberLazyListState(
        cacheWindow = LazyLayoutCacheWindow(
            aheadFraction = 0.5F,
            behindFraction = 0.5F,
        ),
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TraktTheme.colors.backgroundPrimary)
            .nestedScroll(headerState.connection),
    ) {
        val contentPadding = PaddingValues(
            top = WindowInsets.statusBars.asPaddingValues()
                .calculateTopPadding(),
            bottom = WindowInsets.navigationBars.asPaddingValues()
                .calculateBottomPadding()
                .plus(TraktTheme.size.navigationBarHeight * 2),
        )

        ScrollableBackdropImage(
            imageUrl = state.backgroundUrl,
            scrollState = listState,
        )

        ContentList(
            listState = listState,
            listItems = (state.items ?: emptyList()).toImmutableList(),
            listFilters = state.usersFilter,
            contentPadding = contentPadding,
            onTopOfList = { headerState.resetScrolled() },
            onEndOfList = onLoadMore,
            onFilterClick = onFilterClick,
            onBackClick = onBackClick,
        )
    }
}

@Composable
private fun ContentList(
    modifier: Modifier = Modifier,
    listItems: ImmutableList<HomeActivityItem>,
    listFilters: AllActivityState.UsersFilter,
    listState: LazyListState,
    contentPadding: PaddingValues,
    onFilterClick: (User) -> Unit,
    onTopOfList: () -> Unit,
    onEndOfList: () -> Unit,
    onBackClick: () -> Unit,
) {
    val isScrolledToBottom by remember(listItems.size) {
        derivedStateOf {
            listState.firstVisibleItemIndex >= (listItems.size - 5)
        }
    }

    val isScrolledToTop by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex == 0 &&
                listState.firstVisibleItemScrollOffset == 0
        }
    }

    LaunchedEffect(isScrolledToTop) {
        if (isScrolledToTop) {
            onTopOfList()
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
                modifier = Modifier
                    .padding(
                        start = TraktTheme.spacing.mainPageHorizontalSpace,
                        end = TraktTheme.spacing.mainPageHorizontalSpace,
                        bottom = 2.dp,
                    )
                    .onClick {
                        onBackClick()
                    },
            )
        }

        if (listFilters.users.isNotEmpty()) {
            item {
                ContentFilters(
                    state = listFilters,
                    onFilterClick = onFilterClick,
                )
            }
        }

        items(
            items = listItems,
            key = { it.id },
        ) { item ->
            when (item) {
                is MovieItem -> {
                    AllActivityMovieItem(
                        item = item,
                        modifier = Modifier
                            .padding(
                                start = TraktTheme.spacing.mainPageHorizontalSpace,
                                end = TraktTheme.spacing.mainPageHorizontalSpace,
                                bottom = TraktTheme.spacing.mainListVerticalSpace,
                            )
                            .animateItem(
                                fadeInSpec = null,
                                fadeOutSpec = null,
                            ),
                    )
                }
                is EpisodeItem -> {
                    AllActivityEpisodeItem(
                        item = item,
                        modifier = Modifier
                            .padding(
                                start = TraktTheme.spacing.mainPageHorizontalSpace,
                                end = TraktTheme.spacing.mainPageHorizontalSpace,
                                bottom = TraktTheme.spacing.mainListVerticalSpace,
                            )
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
private fun ContentFilters(
    state: AllActivityState.UsersFilter,
    onFilterClick: (User) -> Unit,
) {
    FilterChipGroup(
        paddingHorizontal = PaddingValues(
            start = TraktTheme.spacing.mainPageHorizontalSpace,
            end = TraktTheme.spacing.mainPageHorizontalSpace,
        ),
        paddingVertical = PaddingValues(bottom = 22.dp),
    ) {
        for (user in state.users) {
            UserFilterChip(
                user = user,
                selected = state.selectedUser?.ids?.trakt == user.ids.trakt,
                onClick = { onFilterClick(user) },
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
private fun Preview() {
    TraktTheme {
        AllHomeUpNextContent(
            state = AllHomeUpNextState(
                loading = LoadingState.DONE,
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
        AllHomeUpNextContent(
            state = AllHomeUpNextState(
                loading = LOADING,
            ),
        )
    }
}
