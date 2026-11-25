@file:OptIn(ExperimentalCoilApi::class)

package tv.trakt.trakt.core.lists

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastRoundToInt
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.ColorImage
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import tv.trakt.trakt.LocalSnackbarState
import tv.trakt.trakt.LocalStartAuthorization
import tv.trakt.trakt.common.firebase.FirebaseConfig.RemoteKey.MOBILE_EMPTY_IMAGE_1
import tv.trakt.trakt.common.firebase.FirebaseConfig.RemoteKey.MOBILE_EMPTY_IMAGE_2
import tv.trakt.trakt.common.firebase.FirebaseConfig.RemoteKey.MOBILE_EMPTY_IMAGE_3
import tv.trakt.trakt.common.firebase.FirebaseConfig.RemoteKey.MOBILE_EMPTY_IMAGE_4
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.extensions.DevicePreview
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.common.model.MediaMode
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.lists.CustomList
import tv.trakt.trakt.common.model.lists.ListsItem
import tv.trakt.trakt.common.model.lists.SmartList
import tv.trakt.trakt.common.model.lists.SmartListFilters
import tv.trakt.trakt.common.model.lists.SmartListSource
import tv.trakt.trakt.common.model.lists.SmartListTemplate
import tv.trakt.trakt.common.ui.composables.rememberCacheWindow
import tv.trakt.trakt.core.auth.model.LoginSource
import tv.trakt.trakt.core.filters.GlobalFiltersSheet
import tv.trakt.trakt.core.home.views.HomeEmptyView
import tv.trakt.trakt.core.lists.features.convert.nudge.SmartListNudgeView
import tv.trakt.trakt.core.lists.helpers.shareList
import tv.trakt.trakt.core.lists.sections.personal.model.PersonalListType
import tv.trakt.trakt.core.lists.sections.personal.model.PersonalListType.Collaborations
import tv.trakt.trakt.core.lists.sections.personal.model.PersonalListType.Liked
import tv.trakt.trakt.core.lists.sections.personal.model.PersonalListType.Personal
import tv.trakt.trakt.core.lists.sections.personal.model.PersonalListType.Smart
import tv.trakt.trakt.core.lists.sections.personal.ui.ListsFilters
import tv.trakt.trakt.core.lists.sections.smart.details.recipe.SmartListRecipeSheet
import tv.trakt.trakt.core.lists.sections.smart.templates.SmartListTemplatesState
import tv.trakt.trakt.core.lists.sections.smart.templates.SmartListTemplatesViewModel
import tv.trakt.trakt.core.lists.sections.smart.templates.views.SmartListTemplateCard
import tv.trakt.trakt.core.lists.sections.smart.templates.views.SmartListTemplatesIntro
import tv.trakt.trakt.core.lists.sections.smart.templates.views.SmartListTemplatesLink
import tv.trakt.trakt.core.lists.sections.watchlist.ListsWatchlistView
import tv.trakt.trakt.core.lists.sheets.CreateListSheet
import tv.trakt.trakt.core.lists.sheets.CreateListTypeSheet
import tv.trakt.trakt.core.lists.sheets.EditListSheet
import tv.trakt.trakt.core.lists.sheets.collaborators.LeaveCollaborationConfirmation
import tv.trakt.trakt.core.lists.sheets.collaborators.ManageCollaboratorsSheet
import tv.trakt.trakt.core.lists.sheets.collaborators.showListMovedSnackbar
import tv.trakt.trakt.helpers.ScreenHeaderState
import tv.trakt.trakt.helpers.rememberHeaderState
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.ScrollableBackdropImage
import tv.trakt.trakt.ui.components.TraktSectionHeader
import tv.trakt.trakt.ui.components.chips.FilterChip
import tv.trakt.trakt.ui.components.confirmation.RemoveConfirmationSheet
import tv.trakt.trakt.ui.components.headerbar.HeaderBar
import tv.trakt.trakt.ui.components.mediacards.list.CollaborationListDropdown
import tv.trakt.trakt.ui.components.mediacards.list.CustomListCard
import tv.trakt.trakt.ui.components.mediacards.list.PersonalListDropdown
import tv.trakt.trakt.ui.components.mediacards.list.SmartListCard
import tv.trakt.trakt.ui.components.mediacards.skeletons.CustomListSkeletonCard
import tv.trakt.trakt.ui.theme.HorizontalImageAspectRatio
import tv.trakt.trakt.ui.theme.TraktTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ListsScreen(
    viewModel: ListsViewModel,
    templatesViewModel: SmartListTemplatesViewModel,
    onNavigateToProfile: () -> Unit,
    onNavigateToDiscover: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToShow: (TraktId) -> Unit,
    onNavigateToMovie: (TraktId) -> Unit,
    onNavigateToWatchlist: () -> Unit,
    onNavigateToPersonalList: (CustomList) -> Unit,
    onNavigateToCustomList: (CustomList) -> Unit,
    onNavigateToSmartList: (SmartList) -> Unit,
    onNavigateToCreateSmartList: () -> Unit,
    onNavigateToEditSmartList: (SmartList) -> Unit,
    onNavigateToSmartListTemplate: (SmartListFilters, String) -> Unit,
    onNavigateToSmartListTemplates: () -> Unit,
    onNavigateToListReorder: (CustomList) -> Unit,
    onNavigateToAddFromLists: (CustomList) -> Unit,
    onNavigateToConvertSmartList: (TraktId) -> Unit,
    onNavigateToAllLists: (PersonalListType) -> Unit,
    onNavigateToVip: () -> Unit,
    onNavigateToUser: (User) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val templatesState by templatesViewModel.state.collectAsStateWithLifecycle()

    var createListTypeSheet by remember { mutableStateOf(false) }
    var createListSheet by remember { mutableStateOf(false) }
    var editListSheet by remember { mutableStateOf<CustomList?>(null) }
    var collaboratorsSheet by remember { mutableStateOf<CustomList?>(null) }
    var leaveCollaborationList by remember { mutableStateOf<CustomList?>(null) }
    var filtersSheet by remember { mutableStateOf(false) }
    var smartListToDelete by remember { mutableStateOf<SmartList?>(null) }
    var smartListRecipe by remember { mutableStateOf<SmartList?>(null) }

    val context = LocalContext.current
    val resources = LocalResources.current
    val snackbar = LocalSnackbarState.current
    val startAuthorization = LocalStartAuthorization.current

    LaunchedEffect(state.info) {
        state.info?.let { info ->
            scope.launch {
                snackbar.showSnackbar(message = info.get(context))
            }
            viewModel.clearInfo()
        }
    }

    ListsScreenContent(
        state = state,
        templatesState = templatesState,
        onFilterClick = {
            if (state.filter == it) {
                onNavigateToAllLists(it)
                return@ListsScreenContent
            }
            viewModel.setFilter(it)
        },
        onProfileClick = {
            if (state.user.user == null) {
                startAuthorization(LoginSource.Gated)
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
        onCreateClick = { createListTypeSheet = true },
        onCreateListClick = { createListSheet = true },
        onCreateSmartListClick = onNavigateToCreateSmartList,
        onEditListClick = { editListSheet = it },
        onCollaboratorsClick = { collaboratorsSheet = it },
        onLeaveCollaborationClick = { leaveCollaborationList = it },
        onReorderListClick = onNavigateToListReorder,
        onAddFromListsClick = onNavigateToAddFromLists,
        onConvertSmartListClick = onNavigateToConvertSmartList,
        onShareListClick = { list ->
            viewModel.logListShare()
            shareList(
                user = state.user.user,
                list = list.ids.slug.value,
                context = context,
            )
        },
        onPersonalListClick = onNavigateToPersonalList,
        onCustomListClick = onNavigateToCustomList,
        onSmartListClick = onNavigateToSmartList,
        onSmartListDeleteClick = { smartListToDelete = it },
        onSmartListEditClick = onNavigateToEditSmartList,
        onSmartListRecipeClick = { smartListRecipe = it },
        onSmartListTemplateClick = { template, name -> onNavigateToSmartListTemplate(template.filters, name) },
        onSmartListTemplatesClick = onNavigateToSmartListTemplates,
        onSmartListTemplatesHideClick = templatesViewModel::hide,
        onAllListsClick = { onNavigateToAllLists(state.filter) },
        onVipClick = onNavigateToVip,
        onVipSeen = viewModel::onVipUpsellSeen,
        onUserClick = onNavigateToUser,
        onFiltersClick = {
            filtersSheet = true
        },
    )

    CreateListTypeSheet(
        active = createListTypeSheet,
        onCreateList = { createListSheet = true },
        onCreateSmartList = onNavigateToCreateSmartList,
        onDismiss = { createListTypeSheet = false },
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

    ManageCollaboratorsSheet(
        active = collaboratorsSheet != null,
        list = collaboratorsSheet,
        onListMoved = { list, destination ->
            scope.launch {
                val viewed = snackbar.showListMovedSnackbar(resources, list.name, destination)
                if (viewed) {
                    viewModel.logMovedListView()
                    viewModel.setFilter(destination)
                }
            }
        },
        onDismiss = { collaboratorsSheet = null },
    )

    LeaveCollaborationConfirmation(
        list = leaveCollaborationList,
        onDismiss = { leaveCollaborationList = null },
    )

    GlobalFiltersSheet(
        active = filtersSheet,
        onDismiss = {
            filtersSheet = false
        },
    )

    SmartListRecipeSheet(
        list = smartListRecipe,
        onDismiss = { smartListRecipe = null },
    )

    RemoveConfirmationSheet(
        active = smartListToDelete != null,
        title = stringResource(R.string.confirmation_title_delete_list),
        message = stringResource(
            R.string.warning_prompt_delete_list,
            smartListToDelete?.name.orEmpty(),
        ),
        onYes = {
            smartListToDelete?.let { viewModel.deleteSmartList(it.ids.trakt) }
            smartListToDelete = null
        },
        onNo = { smartListToDelete = null },
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ListsScreenContent(
    state: ListsState,
    modifier: Modifier = Modifier,
    templatesState: SmartListTemplatesState = SmartListTemplatesState(),
    onFilterClick: (PersonalListType) -> Unit = {},
    onProfileClick: () -> Unit = {},
    onShowClick: (TraktId) -> Unit = {},
    onShowsClick: () -> Unit = {},
    onMoviesClick: () -> Unit = {},
    onMovieClick: (TraktId) -> Unit = {},
    onCreateClick: () -> Unit = {},
    onCreateListClick: () -> Unit = {},
    onCreateSmartListClick: () -> Unit = {},
    onSearchListClick: () -> Unit = {},
    onEditListClick: (CustomList) -> Unit = {},
    onCollaboratorsClick: (CustomList) -> Unit = {},
    onLeaveCollaborationClick: (CustomList) -> Unit = {},
    onReorderListClick: (CustomList) -> Unit = {},
    onAddFromListsClick: (CustomList) -> Unit = {},
    onConvertSmartListClick: (TraktId) -> Unit = {},
    onShareListClick: (CustomList) -> Unit = {},
    onWatchlistClick: () -> Unit = {},
    onPersonalListClick: (CustomList) -> Unit = { _ -> },
    onCustomListClick: (CustomList) -> Unit = { _ -> },
    onSmartListClick: (SmartList) -> Unit = { _ -> },
    onSmartListDeleteClick: (SmartList) -> Unit = { _ -> },
    onSmartListEditClick: (SmartList) -> Unit = { _ -> },
    onSmartListRecipeClick: (SmartList) -> Unit = {},
    onSmartListTemplateClick: (SmartListTemplate, String) -> Unit = { _, _ -> },
    onSmartListTemplatesClick: () -> Unit = {},
    onSmartListTemplatesHideClick: () -> Unit = {},
    onAllListsClick: () -> Unit = { },
    onVipClick: () -> Unit = {},
    onVipSeen: () -> Unit = {},
    onFiltersClick: () -> Unit = {},
    onUserClick: (User) -> Unit = {},
) {
    // Watchlist section and header bar resolve dependencies through Koin.
    val preview = LocalInspectionMode.current
    val headerState = rememberHeaderState()
    val lazyListState = rememberLazyListState()

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
        val listVisible = remember(state.lists?.size, state.listsLoading) {
            !state.lists.isNullOrEmpty() && state.listsLoading == Done
        }

        val templatesActive = state.filter == Smart &&
            state.user.isAuthenticated &&
            state.listsLoading == Done
        val templates = templatesState.templates
        val templatesLoading = templatesActive && templates == null
        val templatesStrip = templatesActive &&
            !templates.isNullOrEmpty() &&
            state.lists.isNullOrEmpty() &&
            !templatesState.hidden
        val templatesLink = templatesActive && !templates.isNullOrEmpty() && !templatesStrip

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
            cacheWindow = rememberCacheWindow(0.5F),
            overscrollEffect = null,
            contentPadding = listPadding,
        ) {
            item(
                key = "watchlist",
            ) {
                if (preview) {
                    return@item
                }
                ListsWatchlistView(
                    headerPadding = sectionPadding,
                    contentPadding = sectionPadding,
                    onShowsClick = onShowsClick,
                    onShowClick = onShowClick,
                    onMoviesClick = onMoviesClick,
                    onMovieClick = onMovieClick,
                    onListClick = onCustomListClick,
                    onProfileClick = onProfileClick,
                    onWatchlistClick = onWatchlistClick,
                )
            }

            item(
                key = "personal_header",
            ) {
                MyListsHeader(
                    sectionPadding = sectionPadding,
                    state = state,
                    onHeaderClick = onAllListsClick,
                    onFilterClick = onFilterClick,
                    onCreateClick = onCreateClick,
                    modifier = Modifier.padding(
                        top = TraktTheme.spacing.mainSectionVerticalSpace,
                    ),
                )
            }

            if (!preview && state.filter == Personal && state.user.isAuthenticated) {
                item(key = "smart_list_nudge") {
                    SmartListNudgeView(
                        onConvertClick = onConvertSmartListClick,
                        modifier = Modifier
                            .padding(top = 18.dp)
                            .padding(sectionPadding),
                    )
                }
            }

            val topVerticalPadding = 18.dp
            itemsIndexed(
                items = state.lists ?: emptyList(),
                key = { _, item -> item.id.value },
            ) { index, item ->
                val verticalPadding = when (index) {
                    0 -> topVerticalPadding
                    else -> 16.dp
                }
                val cardModifier = Modifier
                    .padding(
                        top = verticalPadding,
                        start = TraktTheme.spacing.mainPageHorizontalSpace,
                        end = TraktTheme.spacing.mainPageHorizontalSpace,
                    )
                    .aspectRatio(HorizontalImageAspectRatio)

                when (item) {
                    is ListsItem.Smart -> {
                        if (listVisible) {
                            SmartListCard(
                                smartList = item.list,
                                onClick = { onSmartListClick(item.list) },
                                onDeleteClick = { onSmartListDeleteClick(item.list) },
                                onEditClick = { onSmartListEditClick(item.list) },
                                onRecipeClick = { onSmartListRecipeClick(item.list) },
                                modifier = cardModifier,
                            )
                        }
                    }
                    is ListsItem.Custom -> {
                        val list = item.list
                        when (state.filter) {
                            Personal if listVisible -> {
                                CustomListCard(
                                    list = list,
                                    privacyVisible = true,
                                    onClick = { onPersonalListClick(list) },
                                    onUserClick = onUserClick,
                                    modifier = cardModifier,
                                    moreContent = {
                                        PersonalListDropdown(
                                            reorderEnabled = (list.itemCount ?: 0) > 0,
                                            onEditClick = { onEditListClick(list) },
                                            onReorderClick = { onReorderListClick(list) },
                                            onAddFromListsClick = { onAddFromListsClick(list) },
                                            onCollaboratorsClick = { onCollaboratorsClick(list) },
                                            onShareClick = { onShareListClick(list) },
                                        )
                                    },
                                )
                            }
                            Liked if listVisible -> {
                                CustomListCard(
                                    list = list,
                                    liked = true,
                                    likesVisible = true,
                                    onClick = { onCustomListClick(list) },
                                    onUserClick = onUserClick,
                                    modifier = cardModifier,
                                )
                            }
                            Collaborations if listVisible -> {
                                val yours = list.user.ids.trakt == state.user.user?.ids?.trakt
                                CustomListCard(
                                    list = list,
                                    yours = yours,
                                    onClick = {
                                        when {
                                            yours -> onPersonalListClick(list)
                                            else -> onCustomListClick(list)
                                        }
                                    },
                                    onUserClick = onUserClick,
                                    modifier = cardModifier,
                                    moreContent = {
                                        when {
                                            yours -> PersonalListDropdown(
                                                reorderEnabled = (list.itemCount ?: 0) > 0,
                                                onEditClick = { onEditListClick(list) },
                                                onReorderClick = { onReorderListClick(list) },
                                                onAddFromListsClick = { onAddFromListsClick(list) },
                                                onCollaboratorsClick = { onCollaboratorsClick(list) },
                                                onShareClick = { onShareListClick(list) },
                                            )
                                            else -> CollaborationListDropdown(
                                                onLeaveClick = { onLeaveCollaborationClick(list) },
                                            )
                                        }
                                    },
                                )
                            }
                            else -> {}
                        }
                    }
                }
            }

            if (listVisible && state.listsMore) {
                item(key = "view_all") {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp)
                            .padding(sectionPadding),
                    ) {
                        FilterChip(
                            selected = false,
                            text = stringResource(R.string.button_label_view_all_lists),
                            height = 36.dp,
                            paddingHorizontal = PaddingValues(
                                start = 12.dp,
                                end = 16.dp,
                            ),
                            onClick = onAllListsClick,
                        )
                    }
                }
            }

            if (state.lists.isNullOrEmpty() && state.listsLoading == Done) {
                if (templatesStrip) {
                    item(key = "smart_templates") {
                        SmartListTemplatesIntro(
                            onHideClick = onSmartListTemplatesHideClick,
                            modifier = Modifier
                                .padding(top = 16.dp)
                                .padding(sectionPadding),
                        )
                    }

                    items(
                        items = templates,
                        key = { "smart_template_${it.id}" },
                    ) { template ->
                        SmartListTemplateCard(
                            template = template,
                            onClick = { name -> onSmartListTemplateClick(template, name) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    top = 16.dp,
                                    start = TraktTheme.spacing.mainPageHorizontalSpace,
                                    end = TraktTheme.spacing.mainPageHorizontalSpace,
                                ),
                        )
                    }
                } else if (!templatesLoading) {
                    item(key = "empty") {
                        ContentEmptyView(
                            authenticated = state.user.user != null,
                            filter = state.filter,
                            onActionClick = when (state.user.user) {
                                null -> onProfileClick
                                else -> when (state.filter) {
                                    Smart -> onCreateSmartListClick
                                    Personal -> onCreateListClick
                                    Liked, Collaborations -> onSearchListClick
                                }
                            },
                            modifier = Modifier
                                .padding(top = topVerticalPadding)
                                .padding(sectionPadding),
                        )
                    }
                }
            } else if (state.lists.isNullOrEmpty() && state.listsLoading == Loading) {
                item(key = "list_loading") {
                    Column(
                        verticalArrangement = spacedBy(18.dp),
                        modifier = Modifier
                            .padding(top = topVerticalPadding),
                    ) {
                        repeat(3) {
                            CustomListSkeletonCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = TraktTheme.spacing.mainPageHorizontalSpace)
                                    .aspectRatio(HorizontalImageAspectRatio),
                            )
                        }
                    }
                }
            }

            if (templatesLink) {
                item(key = "smart_templates_link") {
                    SmartListTemplatesLink(
                        templates = templates,
                        onClick = onSmartListTemplatesClick,
                        modifier = Modifier
                            .padding(top = 16.dp)
                            .padding(sectionPadding),
                    )
                }
            }
        }

        if (!preview) {
            ListsScreenHeader(
                state = state,
                headerState = headerState,
                isScrolledToTop = isScrolledToTop,
                onVipClick = onVipClick,
                onVipSeen = onVipSeen,
                onFiltersClick = onFiltersClick,
            )
        }
    }
}

@Composable
private fun ListsScreenHeader(
    state: ListsState,
    headerState: ScreenHeaderState,
    isScrolledToTop: Boolean,
    onVipClick: () -> Unit,
    onVipSeen: () -> Unit,
    onFiltersClick: () -> Unit,
) {
    val userState = remember(state.user) {
        val loadingDone = state.user.loading == Done
        val userNotNull = state.user.user != null
        loadingDone to userNotNull
    }

    HeaderBar(
        containerAlpha = if (headerState.scrolled && !isScrolledToTop) 0.98F else 0F,
        showLogin = userState.first && !userState.second,
        showVip = userState.second && state.user.user?.isVip == false,
        showFilters = true,
        onVipSeen = onVipSeen,
        onVipClick = onVipClick,
        onFilterClick = onFiltersClick,
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
    onHeaderClick: () -> Unit,
    onCreateClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth(),
    ) {
        Box(
            modifier = Modifier
                .padding(sectionPadding)
                .fillMaxWidth(),
        ) {
            TraktSectionHeader(
                title = stringResource(R.string.list_title_personal_lists),
                collapsable = false,
                chevron = true,
                modifier = Modifier
                    .onClick(
                        enabled = state.listsLoading != Loading,
                        onClick = onHeaderClick,
                    ),
            )

            Icon(
                painter = painterResource(R.drawable.ic_plus),
                contentDescription = null,
                tint = TraktTheme.colors.textPrimary,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .graphicsLayer {
                        translationX = 1.dp.toPx()
                    }
                    .size(19.dp)
                    .onClick(
                        enabled = state.user.isAuthenticated && !state.listsLoading.isLoading,
                        onClick = onCreateClick,
                    ),
            )
        }

        ListsFilters(
            selected = state.filter,
            onClick = onFilterClick,
            paddingHorizontal = sectionPadding,
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
            filter == Smart -> Firebase.remoteConfig.getString(MOBILE_EMPTY_IMAGE_1).ifBlank { null }
            filter == Personal -> Firebase.remoteConfig.getString(MOBILE_EMPTY_IMAGE_3).ifBlank { null }
            filter == Liked -> Firebase.remoteConfig.getString(MOBILE_EMPTY_IMAGE_4).ifBlank { null }
            filter == Collaborations -> Firebase.remoteConfig.getString(MOBILE_EMPTY_IMAGE_2).ifBlank { null }
            else -> null
        }
    }

    val buttonTextRes = remember(authenticated, filter) {
        when {
            !authenticated -> R.string.button_text_join_trakt
            filter == Smart -> R.string.button_text_create_list
            filter == Personal -> R.string.button_text_create_list
            filter == Liked -> R.string.button_text_toggle_search_lists
            filter == Collaborations -> R.string.button_text_toggle_search_lists
            else -> null
        }
    }

    val buttonIcon = remember(authenticated, filter) {
        when {
            !authenticated -> R.drawable.ic_trakt_icon
            filter in arrayOf(Personal, Smart) -> R.drawable.ic_plus
            filter == Liked -> R.drawable.ic_search_off
            filter == Collaborations -> R.drawable.ic_search_off
            else -> R.drawable.ic_trakt_icon
        }
    }

    HomeEmptyView(
        aspect = HorizontalImageAspectRatio,
        text = stringResource(
            when (filter) {
                Smart -> R.string.text_cta_smart_lists
                Personal -> R.string.text_cta_personal_lists
                Liked -> R.string.text_cta_liked_lists
                Collaborations -> R.string.text_cta_collab_lists
                else -> R.string.text_cta_personal_lists
            },
        ),
        icon = R.drawable.ic_empty_watchlist,
        buttonText = buttonTextRes?.let { stringResource(it) } ?: "",
        buttonIcon = buttonIcon,
        backgroundImageUrl = imageUrl,
        backgroundImage = if (imageUrl == null) R.drawable.ic_splash_background_2 else null,
        onClick = onActionClick,
        modifier = modifier,
    )
}

@DevicePreview
@Composable
private fun Preview() {
    val previewHandler = AsyncImagePreviewHandler { ColorImage(Color.LightGray.toArgb()) }
    TraktTheme {
        CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
            ListsScreenContent(
                state = ListsState(
                    user = ListsState.UserState(
                        user = PreviewData.user1,
                        loading = Done,
                    ),
                    filter = Personal,
                    lists = (1..3)
                        .map {
                            ListsItem.Custom(
                                PreviewData.customList1.copy(
                                    ids = PreviewData.customList1.ids.copy(trakt = TraktId(it)),
                                    name = "My Custom List $it",
                                ),
                            )
                        }
                        .toImmutableList(),
                    listsMore = true,
                    listsLoading = Done,
                ),
            )
        }
    }
}

@DevicePreview
@Composable
private fun PreviewLoading() {
    TraktTheme {
        ListsScreenContent(
            state = ListsState(
                user = ListsState.UserState(
                    user = PreviewData.user1,
                    loading = Done,
                ),
                filter = Personal,
                listsLoading = Loading,
            ),
        )
    }
}

@DevicePreview
@Composable
private fun PreviewEmpty() {
    TraktTheme {
        ListsScreenContent(
            state = ListsState(
                user = ListsState.UserState(loading = Done),
                filter = Smart,
                lists = EmptyImmutableList,
                listsLoading = Done,
            ),
        )
    }
}

@DevicePreview
@Composable
private fun PreviewSmartTemplates() {
    TraktTheme {
        ListsScreenContent(
            state = ListsState(
                user = ListsState.UserState(
                    user = PreviewData.user1,
                    loading = Done,
                ),
                filter = Smart,
                lists = EmptyImmutableList,
                listsLoading = Done,
            ),
            templatesState = SmartListTemplatesState(
                templates = persistentListOf(
                    SmartListTemplate(
                        id = "hidden-gems",
                        filters = SmartListFilters(
                            media = MediaMode.Movies,
                            source = SmartListSource.Discover,
                            ratings = persistentListOf(78),
                            votes = persistentListOf(300, 3000),
                        ),
                        personal = false,
                        total = 941,
                        images = Images(),
                    ),
                ),
            ),
        )
    }
}
