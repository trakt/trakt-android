@file:OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.userprofile

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.layout.LazyLayoutCacheWindow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight.Companion.W500
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.core.layout.WindowSizeClass
import coil3.compose.AsyncImage
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import tv.trakt.trakt.LocalSnackbarState
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.CustomList
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.ui.composables.FilmProgressIndicator
import tv.trakt.trakt.common.ui.theme.colors.Red500
import tv.trakt.trakt.common.ui.theme.colors.Red60
import tv.trakt.trakt.core.lists.sections.personal.model.PersonalListType
import tv.trakt.trakt.core.profile.sections.thismonth.ProfileStatsCard
import tv.trakt.trakt.core.user.model.UserFollowRequest
import tv.trakt.trakt.core.userprofile.sections.favorites.UserProfileFavoritesView
import tv.trakt.trakt.core.userprofile.sections.history.UserProfileHistoryView
import tv.trakt.trakt.core.userprofile.sections.lists.UserProfileListsView
import tv.trakt.trakt.core.userprofile.sections.social.UserProfileSocialView
import tv.trakt.trakt.helpers.SimpleScrollConnection
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.ScrollableBackdropImage
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.components.buttons.TertiaryButton
import tv.trakt.trakt.ui.components.confirmation.RemoveConfirmationSheet
import tv.trakt.trakt.ui.extensions.isAtLeastLarge
import tv.trakt.trakt.ui.theme.DefaultCardShape
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun UserProfileScreen(
    viewModel: UserProfileViewModel,
    onNavigateToShow: (TraktId) -> Unit,
    onNavigateToMovie: (TraktId) -> Unit,
    onNavigateToEpisode: (showId: TraktId, episode: Episode) -> Unit,
    onNavigateToUser: (User) -> Unit,
    onNavigateToList: (CustomList) -> Unit,
    onNavigateToAllHistory: (TraktId) -> Unit,
    onNavigateToAllFavorites: (TraktId) -> Unit,
    onNavigateToAllLists: (User, PersonalListType) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val snack = LocalSnackbarState.current

    var confirmBlockSheet by remember { mutableStateOf(false) }
    var confirmUnfollowSheet by remember { mutableStateOf(false) }

    LaunchedEffect(state.info) {
        state.info?.let { info ->
            scope.launch {
                snack.showSnackbar(message = info.get(context))
            }
            viewModel.clearInfo()
        }
    }

    LaunchedEffect(
        state.navigateShow,
        state.navigateMovie,
        state.navigateEpisode,
    ) {
        state.navigateShow?.let {
            onNavigateToShow(it)
            viewModel.clearNavigation()
        }
        state.navigateEpisode?.let {
            onNavigateToEpisode(it.first, it.second)
            viewModel.clearNavigation()
        }
        state.navigateMovie?.let {
            onNavigateToMovie(it)
            viewModel.clearNavigation()
        }
    }

    UserProfileContent(
        state = state,
        onFollowClick = {
            when {
                state.userFollowing.following -> confirmUnfollowSheet = true
                else -> viewModel.toggleUserFollowed()
            }
        },
        onBlockClick = {
            when {
                state.userBlocked.blocked -> viewModel.toggleUserBlocked()
                else -> confirmBlockSheet = true
            }
        },
        onToggleFollowRequest = { approved ->
            state.userRequest.request?.let { request ->
                viewModel.toggleFollowRequest(
                    request = request,
                    approved = approved,
                )
            }
        },
        onNavigateToShow = viewModel::navigateToShow,
        onNavigateToMovie = viewModel::navigateToMovie,
        onNavigateToEpisode = viewModel::navigateToEpisode,
        onNavigateToUser = onNavigateToUser,
        onNavigateToList = onNavigateToList,
        onNavigateToAllHistory = onNavigateToAllHistory,
        onNavigateToAllFavorites = onNavigateToAllFavorites,
        onNavigateToAllLists = onNavigateToAllLists,
        onNavigateBack = onNavigateBack,
    )

    RemoveConfirmationSheet(
        active = confirmUnfollowSheet,
        onYes = {
            confirmUnfollowSheet = false
            viewModel.toggleUserFollowed()
        },
        onNo = { confirmUnfollowSheet = false },
        title = stringResource(R.string.button_text_unfollow),
        message = stringResource(R.string.warning_prompt_unfollow_user, state.user.displayName),
    )

    RemoveConfirmationSheet(
        active = confirmBlockSheet,
        onYes = {
            confirmBlockSheet = false
            viewModel.toggleUserBlocked()
        },
        onNo = { confirmBlockSheet = false },
        title = stringResource(R.string.warning_prompt_block_user, state.user.displayName),
        message = stringResource(R.string.warning_prompt_block_user_detail),
    )
}

@Composable
private fun UserProfileContent(
    state: UserProfileState,
    onNavigateToShow: (Show) -> Unit = {},
    onNavigateToMovie: (Movie) -> Unit = {},
    onNavigateToEpisode: (Show, Episode) -> Unit = { _, _ -> },
    onNavigateToUser: (User) -> Unit = {},
    onNavigateToList: (CustomList) -> Unit = {},
    onNavigateToAllHistory: (TraktId) -> Unit = {},
    onNavigateToAllFavorites: (TraktId) -> Unit = {},
    onNavigateToAllLists: (User, PersonalListType) -> Unit = { _, _ -> },
    onNavigateBack: () -> Unit = {},
    onToggleFollowRequest: (approved: Boolean) -> Unit = { _ -> },
    onFollowClick: () -> Unit = {},
    onBlockClick: () -> Unit = {},
) {
    val windowClass = currentWindowAdaptiveInfoV2().windowSizeClass

    val sectionPadding = PaddingValues(
        horizontal = TraktTheme.spacing.mainPageHorizontalSpace,
    )

    val listPadding = PaddingValues(
        top = WindowInsets.statusBars.asPaddingValues()
            .calculateTopPadding()
            .plus(10.dp),
        bottom = WindowInsets.navigationBars.asPaddingValues()
            .calculateBottomPadding()
            .plus(TraktTheme.size.navigationBarHeight)
            .plus(TraktTheme.spacing.mainPageBottomSpace),
    )

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
        modifier = Modifier
            .fillMaxSize()
            .background(TraktTheme.colors.backgroundPrimary)
            .nestedScroll(listScrollConnection),
    ) {
        if (!state.user.settings?.coverImage.isNullOrBlank()) {
            ScrollableBackdropImage(
                imageUrl = state.user.settings?.coverImage,
                translation = listScrollConnection.resultOffset,
            )
        }

        LazyColumn(
            state = listState,
            verticalArrangement = spacedBy(0.dp),
            contentPadding = listPadding,
            overscrollEffect = null,
        ) {
            item {
                TitleBar(
                    user = state.user,
                    userBlocked = state.userBlocked,
                    userFollowing = state.userFollowing,
                    onBack = onNavigateBack,
                    onFollowClick = onFollowClick,
                    onBlockClick = onBlockClick,
                    modifier = Modifier
                        .padding(bottom = 8.dp),
                )
            }

            if (state.user.isPrivate && !state.userFollowing.following) {
                if (!state.userFollowing.loading) {
                    userProfilePrivateContent(
                        state = state,
                        windowClass = windowClass,
                        onToggleFollowRequest = onToggleFollowRequest,
                    )
                } else {
                    item {
                        FilmProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 32.dp),
                        )
                    }
                }
            } else {
                userProfilePublicContent(
                    state = state,
                    windowClass = windowClass,
                    sectionPadding = sectionPadding,
                    onToggleFollowRequest = onToggleFollowRequest,
                    onNavigateToShow = onNavigateToShow,
                    onNavigateToEpisode = onNavigateToEpisode,
                    onNavigateToMovie = onNavigateToMovie,
                    onNavigateToAllHistory = onNavigateToAllHistory,
                    onNavigateToAllFavorites = onNavigateToAllFavorites,
                    onNavigateToAllLists = onNavigateToAllLists,
                    onNavigateToList = onNavigateToList,
                    onNavigateToUser = onNavigateToUser,
                )
            }
        }
    }
}

private fun LazyListScope.userProfilePublicContent(
    state: UserProfileState,
    windowClass: WindowSizeClass,
    sectionPadding: PaddingValues,
    onToggleFollowRequest: (approved: Boolean) -> Unit,
    onNavigateToShow: (Show) -> Unit,
    onNavigateToEpisode: (Show, Episode) -> Unit,
    onNavigateToMovie: (Movie) -> Unit,
    onNavigateToAllHistory: (TraktId) -> Unit,
    onNavigateToAllFavorites: (TraktId) -> Unit,
    onNavigateToAllLists: (User, PersonalListType) -> Unit,
    onNavigateToList: (CustomList) -> Unit,
    onNavigateToUser: (User) -> Unit,
) {
    item {
        FollowRequestView(
            visible = state.userRequest.request != null,
            request = state.userRequest.request,
            loading = state.userRequest.loading,
            onApproveClick = { onToggleFollowRequest(true) },
            onDenyClick = { onToggleFollowRequest(false) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = TraktTheme.spacing.mainPageHorizontalSpace),
        )
    }

    if (state.user.isAnyVip) {
        item {
            ProfileStatsCard(
                loading = state.monthStats?.loading ?: true,
                user = state.user,
                stats = state.monthStats?.stats,
                showAllStats = false,
                containerImage = state.monthStats?.backgroundUrl,
                modifier = Modifier
                    .fillMaxWidth(
                        when {
                            windowClass.isAtLeastLarge() -> 0.5F
                            else -> 1F
                        },
                    )
                    .padding(horizontal = TraktTheme.spacing.mainPageHorizontalSpace)
                    .padding(
                        bottom = when {
                            state.user.about.isNullOrBlank() -> TraktTheme.spacing.mainSectionVerticalSpace
                            else -> TraktTheme.spacing.mainSectionVerticalSpace / 1.5F
                        },
                    ),
            )
        }
    }

    if (!state.user.about.isNullOrBlank()) {
        item {
            var expanded by rememberSaveable { mutableStateOf(false) }

            Column(
                verticalArrangement = spacedBy(4.dp),
                modifier = Modifier
                    .padding(horizontal = TraktTheme.spacing.mainPageHorizontalSpace)
                    .padding(bottom = TraktTheme.spacing.mainSectionVerticalSpace)
                    .onClick(throttle = false) { expanded = !expanded },
            ) {
                Text(
                    text = stringResource(R.string.text_about_user, state.user.displayName).uppercase(),
                    color = TraktTheme.colors.textSecondary,
                    style = TraktTheme.typography.heading6.copy(
                        fontWeight = W500,
                        fontSize = 13.sp,
                    ),
                    maxLines = 1,
                    overflow = Ellipsis,
                )
                Text(
                    text = remember(state.user.about) {
                        (state.user.about ?: "").ifEmpty { "-" }
                    },
                    style = TraktTheme.typography.paragraphSmaller.copy(
                        fontSize = 13.sp,
                    ),
                    color = TraktTheme.colors.textPrimary,
                    maxLines = if (expanded) Int.MAX_VALUE else 5,
                    overflow = Ellipsis,
                )
            }
        }
    }

    item {
        UserProfileFavoritesView(
            viewModel = koinViewModel(
                parameters = { parametersOf(state.user.ids.trakt) },
            ),
            headerPadding = sectionPadding,
            contentPadding = sectionPadding,
            onShowClick = onNavigateToShow,
            onMovieClick = onNavigateToMovie,
            onMoreClick = { onNavigateToAllFavorites(state.user.ids.trakt) },
            modifier = Modifier
                .padding(bottom = TraktTheme.spacing.mainSectionVerticalSpace),
        )
    }

    item {
        UserProfileHistoryView(
            viewModel = koinViewModel(
                parameters = { parametersOf(state.user.ids.trakt) },
            ),
            headerPadding = sectionPadding,
            contentPadding = sectionPadding,
            onShowClick = onNavigateToShow,
            onEpisodeClick = onNavigateToEpisode,
            onMovieClick = onNavigateToMovie,
            onMoreClick = { onNavigateToAllHistory(state.user.ids.trakt) },
            modifier = Modifier
                .padding(bottom = TraktTheme.spacing.mainSectionVerticalSpace),
        )
    }

    item {
        UserProfileListsView(
            viewModel = koinViewModel(
                parameters = { parametersOf(state.user.ids.trakt) },
            ),
            headerPadding = sectionPadding,
            contentPadding = sectionPadding,
            onListClick = onNavigateToList,
            onMoreClick = { onNavigateToAllLists(state.user, it) },
            modifier = Modifier
                .padding(bottom = TraktTheme.spacing.mainSectionVerticalSpace),
        )
    }

    item {
        UserProfileSocialView(
            viewModel = koinViewModel(
                parameters = { parametersOf(state.user.ids.trakt) },
            ),
            headerPadding = sectionPadding,
            contentPadding = sectionPadding,
            onUserClick = onNavigateToUser,
            modifier = Modifier
                .padding(bottom = TraktTheme.spacing.mainSectionVerticalSpace),
        )
    }
}

private fun LazyListScope.userProfilePrivateContent(
    windowClass: WindowSizeClass,
    state: UserProfileState,
    onToggleFollowRequest: (approved: Boolean) -> Unit,
) {
    item {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp, CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillParentMaxHeight()
                .fillMaxWidth()
                .padding(horizontal = TraktTheme.spacing.mainPageHorizontalSpace * 2)
                .padding(bottom = TraktTheme.spacing.mainSectionVerticalSpace),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_private_eye),
                contentDescription = null,
                tint = TraktTheme.colors.textPrimary,
                modifier = Modifier.size(56.dp),
            )

            Text(
                text = stringResource(R.string.header_private_profile),
                color = TraktTheme.colors.textPrimary,
                style = TraktTheme.typography.heading5,
            )

            Text(
                text = stringResource(
                    R.string.text_private_profile_description,
                    state.user.displayName,
                ),
                style = TraktTheme.typography.paragraphSmaller.copy(
                    fontSize = 13.sp,
                    fontStyle = FontStyle.Italic,
                ),
                textAlign = TextAlign.Center,
                color = TraktTheme.colors.textSecondary,
                maxLines = if (windowClass.isAtLeastLarge()) Int.MAX_VALUE else 3,
                overflow = Ellipsis,
            )
        }
    }

    item {
        FollowRequestView(
            visible = state.userRequest.request != null,
            request = state.userRequest.request,
            loading = state.userRequest.loading,
            onApproveClick = { onToggleFollowRequest(true) },
            onDenyClick = { onToggleFollowRequest(false) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp)
                .padding(horizontal = TraktTheme.spacing.mainPageHorizontalSpace),
        )
    }
}

@Composable
private fun TitleBar(
    user: User?,
    userBlocked: UserProfileState.BlockedState?,
    userFollowing: UserProfileState.FollowingState?,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onFollowClick: () -> Unit = {},
    onBlockClick: () -> Unit = {},
) {
    Row(
        verticalAlignment = CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxWidth()
            .height(TraktTheme.size.titleBarHeight)
            .padding(
                bottom = 14.dp,
                end = TraktTheme.spacing.mainPageHorizontalSpace,
            ),
    ) {
        Row(
            verticalAlignment = CenterVertically,
            horizontalArrangement = spacedBy(10.dp),
            modifier = Modifier
                .weight(1F, false)
                .padding(
                    start = TraktTheme.spacing.mainPageHorizontalSpace - 2.dp,
                    end = 16.dp,
                )
                .onClick(onClick = onBack),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_back_arrow),
                contentDescription = null,
                tint = TraktTheme.colors.textPrimary,
                modifier = Modifier
                    .graphicsLayer { translationX = -2.dp.toPx() },
            )

            if (user != null) {
                val vipAccent = TraktTheme.colors.vipAccent
                val borderColor = remember(user) {
                    when (user.isAnyVip) {
                        true -> vipAccent
                        else -> Color.White
                    }
                }

                Box(
                    contentAlignment = Alignment.BottomCenter,
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .graphicsLayer {
                                translationY = 1.dp.toPx()
                            },
                    ) {
                        if (user.hasAvatar) {
                            AsyncImage(
                                model = user.images?.avatar?.full,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                error = painterResource(R.drawable.ic_person_placeholder),
                                modifier = Modifier
                                    .border(2.dp, borderColor, CircleShape)
                                    .clip(CircleShape),
                            )
                        } else {
                            Image(
                                painter = painterResource(R.drawable.ic_person_placeholder),
                                contentDescription = null,
                                modifier = Modifier
                                    .border(2.dp, borderColor, CircleShape)
                                    .clip(CircleShape),
                            )
                        }
                    }

                    if (userBlocked?.blocked == true && !userBlocked.loading) {
                        Text(
                            text = stringResource(R.string.tag_text_blocked_user).uppercase(),
                            color = Red500,
                            style = TraktTheme.typography.meta.copy(
                                fontSize = 9.sp,
                                letterSpacing = 0.03.sp,
                            ),
                            modifier = Modifier
                                .graphicsLayer {
                                    translationY = 8.dp.toPx()
                                }
                                .background(
                                    Red60,
                                    RoundedCornerShape(100),
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                        )
                    }
                }

                val subtitle = remember(user) {
                    listOfNotNull(
                        "@${user.username}",
                        user.location?.takeIf { it.isNotBlank() },
                        "VIP".takeIf { user.isAnyVip },
                    ).joinToString(separator = " • ")
                }

                TraktHeader(
                    title = user.displayName,
                    subtitle = subtitle,
                )
            }
        }

        if (user != null) {
            Box {
                var showMenu by remember { mutableStateOf(false) }

                Icon(
                    painter = painterResource(R.drawable.ic_more_vertical),
                    contentDescription = null,
                    tint = TraktTheme.colors.textPrimary,
                    modifier = Modifier
                        .size(18.dp)
                        .onClick { showMenu = true },
                )

                DropdownMenu(
                    expanded = showMenu,
                    containerColor = TraktTheme.colors.dialogContainer,
                    shape = RoundedCornerShape(16.dp),
                    onDismissRequest = { showMenu = false },
                ) {
                    DropdownMenuItem(
                        enabled = userFollowing?.loading != true,
                        text = {
                            Text(
                                text = stringResource(
                                    when (userFollowing?.following) {
                                        true -> R.string.button_text_unfollow
                                        else -> R.string.button_text_follow
                                    },
                                ),
                                style = TraktTheme.typography.buttonTertiary,
                                color = TraktTheme.colors.textPrimary,
                            )
                        },
                        onClick = {
                            onFollowClick()
                            showMenu = false
                        },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(
                                    when (userFollowing?.following) {
                                        true -> R.drawable.ic_person_remove
                                        else -> R.drawable.ic_person_add
                                    },
                                ),
                                contentDescription = null,
                                tint = TraktTheme.colors.textPrimary,
                                modifier = Modifier.size(22.dp),
                            )
                        },
                    )

                    DropdownMenuItem(
                        enabled = userBlocked?.loading != true,
                        text = {
                            Text(
                                text = stringResource(
                                    when {
                                        userBlocked?.blocked == true -> R.string.button_text_unblock
                                        else -> R.string.button_text_block
                                    },
                                ),
                                style = TraktTheme.typography.buttonTertiary,
                                color = TraktTheme.colors.textPrimary,
                            )
                        },
                        onClick = {
                            onBlockClick()
                            showMenu = false
                        },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(R.drawable.ic_block),
                                contentDescription = null,
                                tint = TraktTheme.colors.textPrimary,
                                modifier = Modifier.size(21.dp),
                            )
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun FollowRequestView(
    request: UserFollowRequest?,
    loading: Boolean,
    visible: Boolean,
    modifier: Modifier = Modifier,
    onApproveClick: () -> Unit = {},
    onDenyClick: () -> Unit = {},
) {
    Box(
        contentAlignment = Center,
        modifier = modifier.animateContentSize(
            animationSpec = tween(200, delayMillis = 250),
        ),
    ) {
        if (visible && request != null) {
            Column(
                modifier = Modifier
                    .padding(bottom = TraktTheme.spacing.mainSectionVerticalSpace / 1.5F)
                    .shadow(4.dp, DefaultCardShape)
                    .background(TraktTheme.colors.dialogContainer, DefaultCardShape)
                    .padding(16.dp)
                    .animateContentSize(),
            ) {
                Row(
                    verticalAlignment = CenterVertically,
                    horizontalArrangement = spacedBy(8.dp),
                    modifier = Modifier,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_person_add),
                        contentDescription = null,
                        tint = TraktTheme.colors.textPrimary,
                        modifier = Modifier.size(22.dp),
                    )
                    Text(
                        text = stringResource(R.string.text_info_follow_requested, request.user.displayName),
                        style = TraktTheme.typography.heading5.copy(
                            fontSize = 16.sp,
                            fontWeight = W500,
                        ),
                        color = TraktTheme.colors.textPrimary,
                        maxLines = 2,
                    )
                }

                Row(
                    horizontalArrangement = spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                ) {
                    TertiaryButton(
                        enabled = !loading,
                        text = stringResource(R.string.button_text_reject_follow_request),
                        onClick = onDenyClick,
                        containerColor = TraktTheme.colors.primaryButtonContainerDisabled,
                        modifier = Modifier.weight(1F),
                    )

                    TertiaryButton(
                        enabled = !loading,
                        text = stringResource(R.string.button_text_approve_follow_request),
                        onClick = onApproveClick,
                        modifier = Modifier.weight(1F),
                    )
                }
            }
        }
    }
}

// Previews

@Preview(
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview1() {
    TraktTheme {
        TitleBar(
            user = PreviewData.user1.copy(
                location = "Some Location",
            ),
            userBlocked = UserProfileState.BlockedState(
                blocked = true,
                loading = false,
            ),
            userFollowing = UserProfileState.FollowingState(
                following = true,
                loading = false,
            ),
        )
    }
}

@Preview(
    device = "id:pixel_6",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview2() {
    TraktTheme {
        UserProfileContent(
            state = UserProfileState(
                user = PreviewData.user1,
                userRequest = UserProfileState.UserFollowRequestState(
                    UserFollowRequest(
                        id = 1,
                        requestedAt = nowUtcInstant(),
                        user = PreviewData.user1,
                    ),
                ),
            ),
        )
    }
}
