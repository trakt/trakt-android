package tv.trakt.trakt.core.lists

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.layout.LazyLayoutCacheWindow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastRoundToInt
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import tv.trakt.trakt.common.firebase.FirebaseConfig.RemoteKey.MOBILE_EMPTY_IMAGE_3
import tv.trakt.trakt.common.firebase.FirebaseConfig.RemoteKey.MOBILE_EMPTY_IMAGE_4
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.model.CustomList
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.auth.ConfigAuth
import tv.trakt.trakt.core.home.views.HomeEmptyView
import tv.trakt.trakt.core.lists.ListsConfig.LISTS_FULL_VIEW_LIMIT
import tv.trakt.trakt.core.lists.sections.liked.ListsLikedView
import tv.trakt.trakt.core.lists.sections.personal.ListsPersonalView
import tv.trakt.trakt.core.lists.sections.personal.model.PersonalListType
import tv.trakt.trakt.core.lists.sections.personal.model.PersonalListType.Liked
import tv.trakt.trakt.core.lists.sections.personal.model.PersonalListType.Personal
import tv.trakt.trakt.core.lists.sections.personal.ui.PersonalListsFilters
import tv.trakt.trakt.core.lists.sections.watchlist.ListsWatchlistView
import tv.trakt.trakt.core.lists.sheets.CreateListSheet
import tv.trakt.trakt.core.lists.sheets.EditListSheet
import tv.trakt.trakt.helpers.ScreenHeaderState
import tv.trakt.trakt.helpers.rememberHeaderState
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.ScrollableBackdropImage
import tv.trakt.trakt.ui.components.TraktSectionHeader
import tv.trakt.trakt.ui.components.headerbar.HeaderBar
import tv.trakt.trakt.ui.components.mediacards.CustomListCard
import tv.trakt.trakt.ui.components.mediacards.skeletons.CustomListSkeletonCard
import tv.trakt.trakt.ui.components.vip.VipBanner
import tv.trakt.trakt.ui.theme.HorizontalImageAspectRatio
import tv.trakt.trakt.ui.theme.TraktTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ListsScreen(
    viewModel: ListsViewModel,
    onNavigateToProfile: () -> Unit,
    onNavigateToDiscover: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToShow: (TraktId) -> Unit,
    onNavigateToMovie: (TraktId) -> Unit,
    onNavigateToWatchlist: () -> Unit,
    onNavigateToPersonalList: (CustomList) -> Unit,
    onNavigateToCustomList: (CustomList) -> Unit,
    onNavigateToVip: () -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    var createListSheet by remember { mutableStateOf(false) }
    var editListSheet by remember { mutableStateOf<CustomList?>(null) }

    ListsScreenContent(
        state = state,
        onFilterClick = viewModel::setFilter,
        onProfileClick = {
            if (state.user.user == null) {
                uriHandler.openUri(ConfigAuth.authCodeUrl)
            } else {
                onNavigateToProfile()
            }
        },
        onWatchlistClick = onNavigateToWatchlist,
        onShowsClick = onNavigateToDiscover,
        onShowClick = onNavigateToShow,
        onMoviesClick = onNavigateToDiscover,
        onMovieClick = onNavigateToMovie,
        onSearchListClick = onNavigateToSearch,
        onCreateListClick = { createListSheet = true },
        onEditListClick = { editListSheet = it },
        onPersonalListClick = onNavigateToPersonalList,
        onCustomListClick = onNavigateToCustomList,
        onVipClick = onNavigateToVip,
    )

    CreateListSheet(
        active = createListSheet,
        onListCreated = viewModel::loadData,
        onDismiss = { createListSheet = false },
    )

    EditListSheet(
        active = editListSheet != null,
        list = editListSheet,
        onDismiss = { editListSheet = null },
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ListsScreenContent(
    state: ListsState,
    modifier: Modifier = Modifier,
    onFilterClick: (PersonalListType) -> Unit = {},
    onProfileClick: () -> Unit = {},
    onShowClick: (TraktId) -> Unit = {},
    onShowsClick: () -> Unit = {},
    onMoviesClick: () -> Unit = {},
    onMovieClick: (TraktId) -> Unit = {},
    onCreateListClick: () -> Unit = {},
    onSearchListClick: () -> Unit = {},
    onEditListClick: (CustomList) -> Unit = {},
    onWatchlistClick: () -> Unit = {},
    onPersonalListClick: (CustomList) -> Unit = { _ -> },
    onCustomListClick: (CustomList) -> Unit = { _ -> },
    onVipClick: () -> Unit = {},
) {
    val headerState = rememberHeaderState()
    val lazyListState = rememberLazyListState(
        cacheWindow = LazyLayoutCacheWindow(
            aheadFraction = 0.5F,
            behindFraction = 0.5F,
        ),
    )

    val isScrolledToTop by remember {
        derivedStateOf {
            lazyListState.firstVisibleItemIndex == 0 &&
                lazyListState.firstVisibleItemScrollOffset == 0
        }
    }

    LaunchedEffect(isScrolledToTop) {
        if (isScrolledToTop) {
            headerState.resetScrolled()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TraktTheme.colors.backgroundPrimary)
            .nestedScroll(headerState.connection),
    ) {
        val listFullView = remember(state.lists?.size) {
            (state.lists?.size ?: 0) <= LISTS_FULL_VIEW_LIMIT
        }

        val listVisible = remember(state.lists?.size, state.listsLoading) {
            !state.lists.isNullOrEmpty() && state.listsLoading == DONE
        }

        val listPadding = PaddingValues(
            top = WindowInsets.statusBars.asPaddingValues()
                .calculateTopPadding()
                .plus(TraktTheme.spacing.mainPageTopSpace),
            bottom = WindowInsets.navigationBars.asPaddingValues()
                .calculateBottomPadding()
                .plus(TraktTheme.size.navigationBarHeight)
                .plus(TraktTheme.spacing.mainPageBottomSpace),
        )

        val sectionPadding = PaddingValues(
            start = TraktTheme.spacing.mainPageHorizontalSpace,
            end = TraktTheme.spacing.mainPageHorizontalSpace,
        )

        ScrollableBackdropImage(
            scrollState = lazyListState,
        )

        LazyColumn(
            state = lazyListState,
            overscrollEffect = null,
            contentPadding = listPadding,
        ) {
            item(
                key = "watchlist",
            ) {
                ListsWatchlistView(
                    headerPadding = sectionPadding,
                    contentPadding = sectionPadding,
                    onShowsClick = onShowsClick,
                    onShowClick = onShowClick,
                    onMoviesClick = onMoviesClick,
                    onMovieClick = onMovieClick,
                    onProfileClick = onProfileClick,
                    onWatchlistClick = onWatchlistClick,
                )
            }

            if (state.user.user != null && !state.user.user.isVip) {
                item {
                    VipBanner(
                        modifier = Modifier.padding(sectionPadding),
                        onClick = onVipClick,
                    )
                }
            }

            item(
                key = "personal_header",
            ) {
                MyListsHeader(
                    sectionPadding = sectionPadding,
                    state = state,
                    onFilterClick = onFilterClick,
                    onCreateListClick = onCreateListClick,
                    modifier = Modifier.padding(
                        top = TraktTheme.spacing.mainSectionVerticalSpace,
                    ),
                )
            }

            val topVerticalPadding = 22.dp
            itemsIndexed(
                items = state.lists ?: emptyList(),
                key = { _, list -> list.ids.trakt.value },
            ) { index, list ->
                val verticalPadding = when (index) {
                    0 -> topVerticalPadding
                    else -> when {
                        listFullView -> TraktTheme.spacing.mainSectionVerticalSpace
                        else -> 18.dp
                    }
                }
                when (state.filter) {
                    Personal if listVisible -> {
                        if (listFullView) {
                            ListsPersonalView(
                                viewModel = koinViewModel(
                                    key = list.ids.trakt.value.toString(),
                                    parameters = { parametersOf(list.ids.trakt) },
                                ),
                                headerPadding = sectionPadding,
                                contentPadding = sectionPadding,
                                onShowClick = onShowClick,
                                onMovieClick = onMovieClick,
                                onMoreClick = { onEditListClick(list) },
                                onAllClick = { onPersonalListClick(list) },
                                modifier = Modifier.padding(
                                    top = verticalPadding,
                                ),
                            )
                        } else {
                            CustomListCard(
                                list = list,
                                descriptionVisible = true,
                                moreVisible = true,
                                onClick = { onPersonalListClick(list) },
                                onMoreClick = { onEditListClick(list) },
                                modifier = Modifier
                                    .padding(
                                        top = verticalPadding,
                                        start = TraktTheme.spacing.mainPageHorizontalSpace,
                                        end = TraktTheme.spacing.mainPageHorizontalSpace,
                                    )
                                    .aspectRatio(HorizontalImageAspectRatio),
                            )
                        }
                    }
                    Liked if listVisible -> {
                        if (listFullView) {
                            ListsLikedView(
                                viewModel = koinViewModel(
                                    key = list.ids.trakt.value.toString(),
                                    parameters = { parametersOf(list.ids.trakt) },
                                ),
                                headerPadding = sectionPadding,
                                contentPadding = sectionPadding,
                                onShowClick = onShowClick,
                                onMovieClick = onMovieClick,
                                onAllClick = { onCustomListClick(list) },
                                modifier = Modifier.padding(
                                    top = verticalPadding,
                                ),
                            )
                        } else {
                            CustomListCard(
                                list = list,
                                liked = true,
                                likesVisible = true,
                                onClick = { onCustomListClick(list) },
                                modifier = Modifier
                                    .padding(
                                        top = verticalPadding,
                                        start = TraktTheme.spacing.mainPageHorizontalSpace,
                                        end = TraktTheme.spacing.mainPageHorizontalSpace,
                                    )
                                    .aspectRatio(HorizontalImageAspectRatio),
                            )
                        }
                    }
                    else -> {
                        // Noop
                    }
                }
            }

            if (state.lists.isNullOrEmpty() && state.listsLoading == DONE) {
                item(key = "empty") {
                    ContentEmptyView(
                        authenticated = state.user.user != null,
                        filter = state.filter,
                        onActionClick = when (state.user.user) {
                            null -> onProfileClick
                            else -> when (state.filter) {
                                Personal -> onCreateListClick
                                Liked -> onSearchListClick
                            }
                        },
                        modifier = Modifier
                            .padding(top = topVerticalPadding)
                            .padding(sectionPadding),
                    )
                }
            } else if (state.lists.isNullOrEmpty() && state.listsLoading == LOADING) {
                item(key = "list_loading") {
                    CustomListSkeletonCard(
                        modifier = Modifier
                            .padding(top = topVerticalPadding)
                            .padding(horizontal = TraktTheme.spacing.mainPageHorizontalSpace)
                            .aspectRatio(HorizontalImageAspectRatio),
                    )
                }
            }
        }

        ListsScreenHeader(
            state = state,
            headerState = headerState,
            isScrolledToTop = isScrolledToTop,
            onVipClick = onVipClick,
        )
    }
}

@Composable
private fun ListsScreenHeader(
    state: ListsState,
    headerState: ScreenHeaderState,
    isScrolledToTop: Boolean,
    onVipClick: () -> Unit,
) {
    val userState = remember(state.user) {
        val loadingDone = state.user.loading == DONE
        val userNotNull = state.user.user != null
        loadingDone to userNotNull
    }

    HeaderBar(
        containerAlpha = if (headerState.scrolled && !isScrolledToTop) 0.98F else 0F,
        showLogin = userState.first && !userState.second,
        showVip = userState.second && state.user.user?.isVip == false,
        onVipClick = onVipClick,
        modifier = Modifier.offset {
            IntOffset(0, headerState.connection.barOffset.fastRoundToInt())
        },
    )
}

@Composable
private fun MyListsHeader(
    sectionPadding: PaddingValues,
    state: ListsState,
    onFilterClick: (PersonalListType) -> Unit,
    onCreateListClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(sectionPadding),
    ) {
        TraktSectionHeader(
            title = stringResource(R.string.list_title_personal_lists),
            subtitle = stringResource(R.string.text_sort_recently_updated),
            collapsable = false,
            chevron = false,
            extraIcon = {
                AnimatedVisibility(
                    visible = state.filter == Personal,
                    enter = fadeIn(tween(150)),
                    exit = fadeOut(tween(150)),
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(19.dp)
                        .onClick(
                            enabled = state.user.isAuthenticated &&
                                !state.listsLoading.isLoading,
                            onClick = onCreateListClick,
                        ),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_plus),
                        contentDescription = null,
                        tint = TraktTheme.colors.textPrimary,
                        modifier = Modifier
                            .size(19.dp),
                    )
                }
            },
        )

        PersonalListsFilters(
            selected = state.filter,
            onClick = onFilterClick,
            paddingVertical = PaddingValues(top = 13.dp, bottom = 0.dp),
        )
    }
}

@Composable
private fun ContentEmptyView(
    authenticated: Boolean,
    filter: PersonalListType?,
    modifier: Modifier = Modifier,
    onActionClick: () -> Unit,
) {
    val inspection = LocalInspectionMode.current

    val imageUrl = remember(inspection, filter) {
        when {
            inspection -> null
            filter == Personal -> Firebase.remoteConfig.getString(MOBILE_EMPTY_IMAGE_3).ifBlank { null }
            filter == Liked -> Firebase.remoteConfig.getString(MOBILE_EMPTY_IMAGE_4).ifBlank { null }
            else -> null
        }
    }

    val buttonText = remember(authenticated, filter) {
        when {
            !authenticated -> return@remember R.string.button_text_join_trakt
            filter == Personal -> return@remember R.string.button_text_create_list
            filter == Liked -> return@remember R.string.button_text_toggle_search_lists
            else -> R.drawable.ic_trakt_icon
        }
    }

    val buttonIcon = remember(authenticated, filter) {
        when {
            !authenticated -> R.drawable.ic_trakt_icon
            filter == Personal -> R.drawable.ic_plus
            filter == Liked -> R.drawable.ic_search_off
            else -> R.drawable.ic_trakt_icon
        }
    }

    HomeEmptyView(
        aspect = HorizontalImageAspectRatio,
        text = stringResource(
            when (filter) {
                Personal -> R.string.text_cta_personal_lists
                Liked -> R.string.text_cta_liked_lists
                else -> R.string.text_cta_personal_lists
            },
        ),
        icon = R.drawable.ic_empty_watchlist,
        buttonText = stringResource(buttonText),
        buttonIcon = buttonIcon,
        backgroundImageUrl = imageUrl,
        backgroundImage = if (imageUrl == null) R.drawable.ic_splash_background_2 else null,
        onClick = onActionClick,
        modifier = modifier,
    )
}

@Preview(
    device = "id:pixel_9",
    showBackground = true,
    backgroundColor = 0xFF131517,
    locale = "en",
)
@Composable
private fun Preview() {
    TraktTheme {
        ListsScreenContent(
            state = ListsState(),
        )
    }
}
