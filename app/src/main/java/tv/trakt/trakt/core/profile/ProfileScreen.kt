@file:OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.profile

import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.layout.LazyLayoutCacheWindow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight.Companion.W500
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastRoundToInt
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import timber.log.Timber
import tv.trakt.trakt.common.Config.WEB_V3_BASE_URL
import tv.trakt.trakt.common.Config.webYearReviewUrl
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.extensions.toMarkdownText
import tv.trakt.trakt.common.helpers.extensions.toShareUrl
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.core.profile.sections.activity.ProfileActivityView
import tv.trakt.trakt.core.profile.sections.favorites.ProfileFavoritesView
import tv.trakt.trakt.core.profile.sections.history.ProfileHistoryView
import tv.trakt.trakt.core.profile.sections.leaderboard.ui.LeaderboardPill
import tv.trakt.trakt.core.profile.sections.library.ProfileLibraryView
import tv.trakt.trakt.core.profile.sections.progress.ProfileProgressView
import tv.trakt.trakt.core.profile.sections.screentime.ProfileScreenTimeView
import tv.trakt.trakt.core.profile.sections.social.ProfileSocialView
import tv.trakt.trakt.core.profile.sections.thismonth.ProfileStatsCard
import tv.trakt.trakt.core.profile.sections.thismonth.all.AllTimeStatsSheet
import tv.trakt.trakt.helpers.ScreenHeaderState
import tv.trakt.trakt.helpers.SimpleScrollConnection
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.helpers.rememberHeaderState
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.ScrollableBackdropImage
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.components.headerbar.HeaderBar
import tv.trakt.trakt.ui.extensions.isAtLeastLarge
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun ProfileScreen(
    viewModel: ProfileViewModel,
    onNavigateToShow: (TraktId) -> Unit,
    onNavigateToMovie: (TraktId) -> Unit,
    onNavigateToEpisode: (showId: TraktId, episode: Episode) -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToFavorites: () -> Unit,
    onNavigateToProgress: () -> Unit,
    onNavigateToActivity: () -> Unit,
    onNavigateToLibrary: () -> Unit,
    onNavigateToDiscover: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToUser: (User) -> Unit,
    onNavigateToSocial: () -> Unit,
    onNavigateToLeaderboard: () -> Unit,
    onNavigateToVip: () -> Unit,
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    val state by viewModel.state.collectAsStateWithLifecycle()

    var allTimeStatsSheet by remember { mutableStateOf(false) }

    ProfileScreen(
        state = state,
        onNavigateToShow = onNavigateToShow,
        onNavigateToMovie = onNavigateToMovie,
        onNavigateToEpisode = onNavigateToEpisode,
        onNavigateToHistory = onNavigateToHistory,
        onNavigateToFavorites = onNavigateToFavorites,
        onNavigateToProgress = onNavigateToProgress,
        onNavigateToActivity = onNavigateToActivity,
        onNavigateToLibrary = onNavigateToLibrary,
        onNavigateToShows = onNavigateToDiscover,
        onSettingsClick = onNavigateToSettings,
        onNavigateToUser = onNavigateToUser,
        onNavigateToSocial = onNavigateToSocial,
        onLeaderboardClick = onNavigateToLeaderboard,
        onVipClick = onNavigateToVip,
        onVipSeen = viewModel::onVipUpsellSeen,
        onAllTimeStatsClick = {
            viewModel.logAllTimeStatsOpen()
            allTimeStatsSheet = true
        },
        onShareClick = {
            viewModel.logShare()
            shareProfile(
                user = state.user,
                context = context,
            )
        },
    )

    AllTimeStatsSheet(
        stats = state.monthStats,
        isVip = state.user?.isAnyVip == true,
        visible = allTimeStatsSheet,
        onYearClick = { year ->
            viewModel.logAllTimeStatsLink(target = "/users/me/year/$year")
            state.user?.let {
                uriHandler.openUri(webYearReviewUrl(user = it.ids.slug.value, year = year))
            }
        },
        onVipClick = {
            allTimeStatsSheet = false
            onNavigateToVip()
        },
        onDismiss = { allTimeStatsSheet = false },
    )
}

@Composable
private fun ProfileScreen(
    state: ProfileState,
    modifier: Modifier = Modifier,
    onNavigateToShow: (TraktId) -> Unit = {},
    onNavigateToMovie: (TraktId) -> Unit = {},
    onNavigateToEpisode: (showId: TraktId, episode: Episode) -> Unit = { _, _ -> },
    onNavigateToHistory: () -> Unit = {},
    onNavigateToFavorites: () -> Unit = {},
    onNavigateToProgress: () -> Unit = {},
    onNavigateToActivity: () -> Unit = {},
    onNavigateToLibrary: () -> Unit = {},
    onNavigateToShows: () -> Unit = {},
    onNavigateToUser: (User) -> Unit = {},
    onNavigateToSocial: () -> Unit = {},
    onLeaderboardClick: () -> Unit = {},
    onVipClick: () -> Unit = {},
    onVipSeen: () -> Unit = {},
    onAllTimeStatsClick: () -> Unit = {},
    onShareClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
) {
    val preview = LocalInspectionMode.current
    val windowClass = currentWindowAdaptiveInfo().windowSizeClass

    val headerState = rememberHeaderState()

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

    val listState = rememberLazyListState(
        cacheWindow = LazyLayoutCacheWindow(
            aheadFraction = 0.5F,
            behindFraction = 0.5F,
        ),
    )

    val backdropScrollConnection = rememberSaveable(saver = SimpleScrollConnection.Saver) {
        SimpleScrollConnection()
    }

    val isScrolledToTop by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex == 0 &&
                listState.firstVisibleItemScrollOffset == 0
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
            .nestedScroll(headerState.connection)
            .nestedScroll(backdropScrollConnection),
    ) {
        ScrollableBackdropImage(
            translation = backdropScrollConnection.resultOffset,
        )

        LazyColumn(
            state = listState,
            verticalArrangement = spacedBy(0.dp),
            contentPadding = listPadding,
            overscrollEffect = null,
        ) {
            item {
                TitleBar(
                    user = state.user,
                    leaderboardVisible = state.hasLeaderboard,
                    onLeaderboardClick = onLeaderboardClick,
                    modifier = Modifier
                        .padding(bottom = 8.dp),
                )
            }

            if (state.user != null) {
                if (!state.user.about.isNullOrBlank()) {
                    item {
                        Column(
                            verticalArrangement = spacedBy(4.dp),
                            modifier = Modifier
                                .padding(horizontal = TraktTheme.spacing.mainPageHorizontalSpace)
                                .padding(bottom = TraktTheme.spacing.mainSectionVerticalSpace / 1.5F),
                        ) {
                            Text(
                                text = stringResource(R.string.text_about).uppercase(),
                                color = TraktTheme.colors.textPrimary,
                                style = TraktTheme.typography.heading6.copy(
                                    fontWeight = W500,
                                    fontSize = 13.sp,
                                ),
                                maxLines = 1,
                                overflow = Ellipsis,
                            )
                            val linkColor = TraktTheme.colors.textPrimary
                            Text(
                                text = remember(state.user.about, linkColor) {
                                    (state.user.about ?: "").toMarkdownText(linkColor)
                                },
                                style = TraktTheme.typography.paragraphSmaller.copy(
                                    fontSize = 13.sp,
                                ),
                                color = TraktTheme.colors.textSecondary,
                                maxLines = 3,
                                overflow = Ellipsis,
                            )
                        }
                    }
                }

                item {
                    ProfileStatsCard(
                        user = state.user,
                        stats = state.monthStats,
                        containerImage = state.monthBackgroundUrl,
                        onAllTimeStatsClick = onAllTimeStatsClick,
                        modifier = Modifier
                            .fillMaxWidth(
                                when {
                                    windowClass.isAtLeastLarge() -> 0.5F
                                    else -> 1F
                                },
                            )
                            .padding(horizontal = TraktTheme.spacing.mainPageHorizontalSpace)
                            .padding(bottom = TraktTheme.spacing.mainSectionVerticalSpace - 10.dp),
                    )
                }

                if (!preview) {
                    item {
                        ProfileFavoritesView(
                            headerPadding = sectionPadding,
                            contentPadding = sectionPadding,
                            onShowClick = onNavigateToShow,
                            onMovieClick = onNavigateToMovie,
                            onMoreClick = onNavigateToFavorites,
                            onShowsClick = onNavigateToShows,
                            modifier = Modifier
                                .padding(bottom = TraktTheme.spacing.mainSectionVerticalSpace),
                        )
                    }

                    item {
                        ProfileScreenTimeView(
                            headerPadding = sectionPadding,
                            contentPadding = sectionPadding,
                            modifier = Modifier
                                .padding(
                                    bottom = TraktTheme.spacing.mainSectionVerticalSpace -
                                        TraktTheme.spacing.shadowClipSpace,
                                ),
                        )
                    }

                    item {
                        ProfileHistoryView(
                            headerPadding = sectionPadding,
                            contentPadding = sectionPadding,
                            onMoreClick = onNavigateToHistory,
                            onEpisodeClick = onNavigateToEpisode,
                            onShowClick = onNavigateToShow,
                            onMovieClick = onNavigateToMovie,
                            modifier = Modifier
                                .padding(bottom = TraktTheme.spacing.mainSectionVerticalSpace),
                        )
                    }

                    item {
                        ProfileActivityView(
                            headerPadding = sectionPadding,
                            contentPadding = sectionPadding,
                            gifPaused = listState.isScrollInProgress,
                            onMovieClick = onNavigateToMovie,
                            onShowClick = onNavigateToShow,
                            onEpisodeClick = onNavigateToEpisode,
                            onMoreClick = onNavigateToActivity,
                            onEmptyClick = onNavigateToHistory,
                            modifier = Modifier
                                .padding(
                                    bottom = TraktTheme.spacing.mainSectionVerticalSpace -
                                        TraktTheme.spacing.shadowClipSpace,
                                ),
                        )
                    }

                    if (state.mode.isMediaOrShows) {
                        item {
                            ProfileProgressView(
                                headerPadding = sectionPadding,
                                contentPadding = sectionPadding,
                                onShowClick = onNavigateToShow,
                                onMoreClick = onNavigateToProgress,
                                modifier = Modifier
                                    .padding(bottom = TraktTheme.spacing.mainSectionVerticalSpace),
                            )
                        }
                    }

                    item {
                        ProfileLibraryView(
                            headerPadding = sectionPadding,
                            contentPadding = sectionPadding,
                            onShowClick = onNavigateToShow,
                            onMovieClick = onNavigateToMovie,
                            onMoreClick = onNavigateToLibrary,
                            onVipClick = onVipClick,
                            modifier = Modifier
                                .padding(bottom = TraktTheme.spacing.mainSectionVerticalSpace),
                        )
                    }

                    item {
                        ProfileSocialView(
                            headerPadding = sectionPadding,
                            contentPadding = sectionPadding,
                            onUserClick = onNavigateToUser,
                            onSeeAllClick = onNavigateToSocial,
                            modifier = Modifier
                                .padding(bottom = TraktTheme.spacing.mainSectionVerticalSpace),
                        )
                    }
                }
            }
        }

        ProfileScreenHeader(
            state = state,
            headerState = headerState,
            isScrolledToTop = isScrolledToTop,
            onVipClick = onVipClick,
            onVipSeen = onVipSeen,
            onShareClick = onShareClick,
            onSettingsClick = onSettingsClick,
        )
    }
}

@Composable
private fun ProfileScreenHeader(
    state: ProfileState,
    headerState: ScreenHeaderState,
    isScrolledToTop: Boolean,
    onVipClick: () -> Unit,
    onVipSeen: () -> Unit,
    onShareClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    HeaderBar(
        containerAlpha = if (headerState.scrolled && !isScrolledToTop) 0.98F else 0F,
        showVip = state.user?.isVip == false,
        onVipSeen = onVipSeen,
        onVipClick = onVipClick,
        actions = {
            if (state.user != null) {
                ProfileHeaderActions(
                    onShareClick = onShareClick,
                    onSettingsClick = onSettingsClick,
                )
            }
        },
        modifier = Modifier.offset {
            IntOffset(0, headerState.connection.barOffset.fastRoundToInt())
        },
    )
}

@Composable
private fun TitleBar(
    user: User?,
    modifier: Modifier = Modifier,
    leaderboardVisible: Boolean = false,
    onLeaderboardClick: () -> Unit = {},
) {
    val avatarSize = 64.dp
    Row(
        verticalAlignment = CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxWidth()
            .padding(
                bottom = 13.dp,
                end = TraktTheme.spacing.mainPageHorizontalSpace,
            ),
    ) {
        if (user != null) {
            Row(
                verticalAlignment = CenterVertically,
                horizontalArrangement = spacedBy(10.dp),
                modifier = Modifier
                    .padding(
                        start = TraktTheme.spacing.mainPageHorizontalSpace - 2.dp,
                    ),
            ) {
                Box(
                    contentAlignment = Alignment.CenterStart,
                ) {
                    val vipAccent = TraktTheme.colors.vipAccent
                    val borderColor = remember(user) {
                        when (user.isAnyVip) {
                            true -> vipAccent
                            else -> Color.White
                        }
                    }

                    if (user.hasAvatar) {
                        AsyncImage(
                            model = user.images?.avatar?.full,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            error = painterResource(R.drawable.ic_person_placeholder),
                            modifier = Modifier
                                .size(avatarSize)
                                .border(2.dp, borderColor, CircleShape)
                                .clip(CircleShape),
                        )
                    } else {
                        Image(
                            painter = painterResource(R.drawable.ic_person_placeholder),
                            contentDescription = null,
                            modifier = Modifier
                                .size(avatarSize)
                                .border(2.dp, borderColor, CircleShape)
                                .clip(CircleShape),
                        )
                    }
                }

                TraktHeader(
                    title = user.displayName,
                    subtitle = remember(user) {
                        val vip = when (user.isAnyVip) {
                            true -> "VIP • "
                            else -> ""
                        }
                        when {
                            user.location.isNullOrBlank() && vip.isBlank() -> null
                            else -> "$vip${user.location.orEmpty()}"
                        }
                    },
                )
            }

            AnimatedVisibility(
                visible = leaderboardVisible,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                LeaderboardPill(
                    onClick = onLeaderboardClick,
                )
            }
        }
    }
}

@Composable
private fun ProfileHeaderActions(
    onShareClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    Row(
        verticalAlignment = CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(22.dp),
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_share),
            contentDescription = null,
            tint = TraktTheme.colors.textPrimary,
            modifier = Modifier
                .size(22.dp)
                .onClick(onClick = onShareClick),
        )

        Icon(
            painter = painterResource(R.drawable.ic_settings),
            contentDescription = null,
            tint = TraktTheme.colors.textPrimary,
            modifier = Modifier
                .padding(start = 1.5.dp)
                .size(22.dp)
                .graphicsLayer {
                    translationY = 0.25.dp.toPx()
                }
                .onClick(onClick = onSettingsClick),
        )
    }
}

private fun shareProfile(
    user: User?,
    context: Context,
) {
    if (user == null) {
        Timber.e("Unable to share profile: user is null")
        return
    }

    val shareText = "${WEB_V3_BASE_URL}profile/${user.ids.slug.value}"
        .toShareUrl(user.settings?.shareCode)
    val intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, shareText)
        type = "text/plain"
    }

    context.startActivity(Intent.createChooser(intent, user.displayName))
}

// Previews

@PreviewTraktLightDark
@Composable
private fun Preview() {
    TraktThemePreview {
        ProfileScreen(
            state = ProfileState(
                user = PreviewData.user1,
                hasLeaderboard = true,
            ),
        )
    }
}
