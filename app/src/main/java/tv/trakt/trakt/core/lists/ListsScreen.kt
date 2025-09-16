package tv.trakt.trakt.core.lists

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.layout.LazyLayoutCacheWindow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.util.fastRoundToInt
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import tv.trakt.trakt.LocalBottomBarVisibility
import tv.trakt.trakt.common.firebase.FirebaseConfig.RemoteKey.MOBILE_EMPTY_IMAGE_3
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.model.CustomList
import tv.trakt.trakt.core.home.views.HomeEmptyView
import tv.trakt.trakt.core.lists.sections.personal.ListsPersonalView
import tv.trakt.trakt.core.lists.sections.watchlist.ListsWatchlistView
import tv.trakt.trakt.core.lists.sheets.CreateListSheet
import tv.trakt.trakt.core.lists.sheets.EditListSheet
import tv.trakt.trakt.helpers.ScreenHeaderState
import tv.trakt.trakt.helpers.rememberHeaderState
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.BackdropImage
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.components.buttons.TertiaryButton
import tv.trakt.trakt.ui.components.headerbar.HeaderBar
import tv.trakt.trakt.ui.theme.TraktTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ListsScreen(
    viewModel: ListsViewModel,
    onNavigateToProfile: () -> Unit,
    onNavigateToShows: () -> Unit,
    onNavigateToMovies: () -> Unit,
) {
    val localBottomBarVisibility = LocalBottomBarVisibility.current
    LaunchedEffect(Unit) {
        localBottomBarVisibility.value = true
    }

    val state by viewModel.state.collectAsStateWithLifecycle()
    var createListSheet by remember { mutableStateOf(false) }
    var editListSheet by remember { mutableStateOf<CustomList?>(null) }

    ListsScreenContent(
        state = state,
        onProfileClick = onNavigateToProfile,
        onShowsClick = onNavigateToShows,
        onMoviesClick = onNavigateToMovies,
        onCreateListClick = { createListSheet = true },
        onEditListClick = { editListSheet = it },
    )

    CreateListSheet(
        sheetActive = createListSheet,
        onListCreated = viewModel::loadData,
        onDismiss = { createListSheet = false },
    )

    EditListSheet(
        sheetActive = editListSheet != null,
        list = editListSheet,
        onListEdited = viewModel::loadData,
        onListDeleted = viewModel::loadData,
        onDismiss = { editListSheet = null },
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ListsScreenContent(
    state: ListsState,
    modifier: Modifier = Modifier,
    onProfileClick: () -> Unit = {},
    onShowsClick: () -> Unit = {},
    onMoviesClick: () -> Unit = {},
    onCreateListClick: () -> Unit = {},
    onEditListClick: (CustomList) -> Unit = {},
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
        BackdropImage(
            imageUrl = state.backgroundUrl,
            modifier = Modifier.graphicsLayer {
                if (lazyListState.firstVisibleItemIndex == 0) {
                    translationY = (-0.75F * lazyListState.firstVisibleItemScrollOffset)
                } else {
                    alpha = 0F
                }
            },
        )

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

        LazyColumn(
            state = lazyListState,
            overscrollEffect = null,
            verticalArrangement = spacedBy(TraktTheme.spacing.mainSectionVerticalSpace),
            contentPadding = listPadding,
        ) {
            item(
                key = "watchlist",
            ) {
                ListsWatchlistView(
                    headerPadding = sectionPadding,
                    contentPadding = sectionPadding,
                    onShowsClick = onShowsClick,
                    onMoviesClick = onMoviesClick,
                    onProfileClick = onProfileClick,
                )
            }

            item(
                key = "personal_header",
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(sectionPadding),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TraktHeader(
                        title = stringResource(R.string.list_title_personal_lists),
                        subtitle = stringResource(R.string.text_sort_recently_updated),
                    )

                    if (!state.lists.isNullOrEmpty()) {
                        TertiaryButton(
                            text = stringResource(R.string.button_text_create_list),
                            icon = painterResource(R.drawable.ic_plus_round),
                            onClick = onCreateListClick,
                        )
                    }
                }
            }

            items(
                items = state.lists ?: emptyList(),
                key = { list -> list.ids.trakt.value },
            ) { list ->
                ListsPersonalView(
                    list = list,
                    viewModel = koinViewModel(
                        key = list.ids.trakt.value.toString(),
                        parameters = { parametersOf(list.ids.trakt) },
                    ),
                    headerPadding = sectionPadding,
                    contentPadding = sectionPadding,
                    onMoreClick = { onEditListClick(list) },
                )
            }

            if (state.lists.isNullOrEmpty()) {
                item(key = "empty") {
                    ContentEmptyView(
                        authenticated = state.user.user != null,
                        modifier = Modifier.padding(sectionPadding),
                        onActionClick = if (state.user.user == null) {
                            onProfileClick
                        } else {
                            onCreateListClick
                        },
                    )
                }
            }
        }

        ListsScreenHeader(
            state = state,
            headerState = headerState,
            isScrolledToTop = isScrolledToTop,
            onProfileClick = onProfileClick,
        )
    }
}

@Composable
private fun ListsScreenHeader(
    state: ListsState,
    headerState: ScreenHeaderState,
    isScrolledToTop: Boolean,
    onProfileClick: () -> Unit,
) {
    val userState = remember(state.user) {
        val loadingDone = state.user.loading == DONE
        val userNotNull = state.user.user != null
        loadingDone to userNotNull
    }

    HeaderBar(
        containerAlpha = if (headerState.scrolled && !isScrolledToTop) 0.98F else 0F,
        showVip = headerState.startScrolled,
        showProfile = userState.first && userState.second,
        showJoinTrakt = userState.first && !userState.second,
        userVip = state.user.user?.isAnyVip ?: false,
        userAvatar = state.user.user?.images?.avatar?.full,
        onJoinClick = onProfileClick,
        onProfileClick = onProfileClick,
        modifier = Modifier.offset {
            IntOffset(0, headerState.connection.barOffset.fastRoundToInt())
        },
    )
}

@Composable
private fun ContentEmptyView(
    authenticated: Boolean,
    modifier: Modifier = Modifier,
    onActionClick: () -> Unit = {},
) {
    val imageUrl = remember {
        Firebase.remoteConfig.getString(MOBILE_EMPTY_IMAGE_3).ifBlank { null }
    }

    val buttonText = remember(authenticated) {
        if (!authenticated) {
            return@remember R.string.button_text_join_trakt
        }
        R.string.page_title_create_list
    }

    val buttonIcon = remember(authenticated) {
        when {
            authenticated -> R.drawable.ic_plus_round
            else -> R.drawable.ic_trakt_icon
        }
    }

    HomeEmptyView(
        text = stringResource(R.string.text_cta_personal_lists),
        icon = R.drawable.ic_empty_upnext,
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
)
@Composable
private fun Preview() {
    TraktTheme {
        ListsScreenContent(
            state = ListsState(),
        )
    }
}
