@file:OptIn(ExperimentalFoundationApi::class)

package tv.trakt.trakt.core.profile

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.layout.LazyLayoutCacheWindow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.core.profile.sections.favorites.ProfileFavoritesView
import tv.trakt.trakt.core.profile.sections.history.ProfileHistoryView
import tv.trakt.trakt.core.profile.sections.social.ProfileSocialView
import tv.trakt.trakt.core.profile.sections.thismonth.ThisMonthCard
import tv.trakt.trakt.helpers.SimpleScrollConnection
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.ScrollableBackdropImage
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.components.confirmation.ConfirmationSheet
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun ProfileScreen(
    viewModel: ProfileViewModel,
    onNavigateToShow: (TraktId) -> Unit,
    onNavigateToMovie: (TraktId) -> Unit,
    onNavigateToEpisode: (showId: TraktId, episode: Episode) -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToFavorites: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    var confirmLogout by remember { mutableStateOf(false) }

    LaunchedEffect(state.user) {
        if (state.loading == DONE && state.user == null) {
            onNavigateBack()
        }
    }

    ProfileScreenContent(
        state = state,
        onNavigateToShow = onNavigateToShow,
        onNavigateToMovie = onNavigateToMovie,
        onNavigateToEpisode = onNavigateToEpisode,
        onNavigateToHistory = onNavigateToHistory,
        onNavigateToFavorites = onNavigateToFavorites,
        onLogoutClick = { confirmLogout = true },
        onBackClick = onNavigateBack,
    )

    @OptIn(ExperimentalMaterial3Api::class)
    ConfirmationSheet(
        active = confirmLogout,
        onYes = {
            confirmLogout = false
            viewModel.logoutUser()
        },
        onNo = {
            confirmLogout = false
        },
        title = stringResource(R.string.button_text_logout),
        message = stringResource(R.string.warning_prompt_logout),
    )
}

@Composable
private fun ProfileScreenContent(
    state: ProfileState,
    modifier: Modifier = Modifier,
    onNavigateToShow: (TraktId) -> Unit = {},
    onNavigateToMovie: (TraktId) -> Unit = {},
    onNavigateToEpisode: (showId: TraktId, episode: Episode) -> Unit = { _, _ -> },
    onNavigateToHistory: () -> Unit = {},
    onNavigateToFavorites: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    onBackClick: () -> Unit = {},
) {
    val listPadding = PaddingValues(
        top = WindowInsets.statusBars.asPaddingValues()
            .calculateTopPadding()
            .plus(10.dp),
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

    val listScrollConnection = rememberSaveable(saver = SimpleScrollConnection.Saver) {
        SimpleScrollConnection()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TraktTheme.colors.backgroundPrimary)
            .nestedScroll(listScrollConnection),
    ) {
        ScrollableBackdropImage(
            imageUrl = state.backgroundUrl,
            scrollState = listState,
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
                    onLogoutClick = onLogoutClick,
                    onBackClick = onBackClick,
                    modifier = Modifier
                        .padding(bottom = 14.dp),
                )
            }

            if (state.user != null) {
                item {
                    ThisMonthCard(
                        user = state.user,
                        stats = state.monthStats,
                        containerImage = state.monthBackgroundUrl,
                        modifier = Modifier
                            .padding(horizontal = TraktTheme.spacing.mainPageHorizontalSpace)
                            .padding(bottom = TraktTheme.spacing.mainSectionVerticalSpace),
                    )
                }

//                if (!state.user.about.isNullOrBlank()) {
//                    item {
//                        Column(
//                            verticalArrangement = spacedBy(6.dp),
//                            modifier = Modifier
//                                .padding(horizontal = TraktTheme.spacing.mainPageHorizontalSpace)
//                                .padding(bottom = TraktTheme.spacing.mainSectionVerticalSpace),
//                        ) {
//                            TraktHeader(
//                                title = stringResource(R.string.page_title_about_me),
//                                titleColor = TraktTheme.colors.textSecondary,
//                            )
//                            Text(
//                                text = state.user.about ?: "",
//                                style = TraktTheme.typography.paragraphSmaller,
//                                color = TraktTheme.colors.textPrimary,
//                                maxLines = 3,
//                                textAlign = TextAlign.Center,
//                                overflow = Ellipsis,
//                            )
//                        }
//                    }
//                }

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
                    ProfileFavoritesView(
                        headerPadding = sectionPadding,
                        contentPadding = sectionPadding,
                        onShowClick = onNavigateToShow,
                        onMovieClick = onNavigateToMovie,
                        onMoreClick = onNavigateToFavorites,
                        modifier = Modifier
                            .padding(bottom = TraktTheme.spacing.mainSectionVerticalSpace),
                    )
                }

                item {
                    ProfileSocialView(
                        headerPadding = sectionPadding,
                        contentPadding = sectionPadding,
                        modifier = Modifier
                            .padding(bottom = TraktTheme.spacing.mainSectionVerticalSpace),
                    )
                }
            }
//            } else {
//                item {
//                    TertiaryButton(
//                        text = stringResource(R.string.button_text_login),
//                        icon = painterResource(R.drawable.ic_trakt_icon),
//                        height = 42.dp,
//                        enabled = !state.loading.isLoading,
//                        loading = state.loading.isLoading,
//                        onClick = {
//                            uriHandler.openUri(ConfigAuth.authCodeUrl)
//                        },
//                        modifier = Modifier
//                            .padding(top = 16.dp)
//                            .widthIn(112.dp),
//                    )
//                }
//            }
        }
    }
}

@Composable
private fun TitleBar(
    user: User?,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
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
            horizontalArrangement = spacedBy(12.dp),
            modifier = Modifier
                .onClick { onBackClick() }
                .padding(
                    start = TraktTheme.spacing.mainPageHorizontalSpace - 2.dp,
                ),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_back_arrow),
                tint = Color.White,
                contentDescription = "Back",
            )

            TraktHeader(
                title = user?.displayName ?: stringResource(R.string.page_title_profile),
                subtitle = user?.location,
            )
        }

        if (user != null) {
            Row(
                verticalAlignment = CenterVertically,
                horizontalArrangement = spacedBy(16.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_logout),
                    contentDescription = null,
                    tint = TraktTheme.colors.textPrimary,
                    modifier = Modifier
                        .size(24.dp)
                        .onClick(onClick = onLogoutClick),
                )

                Box(
                    modifier = Modifier.size(34.dp),
                ) {
                    val borderColor = remember(user) {
                        when (user.isAnyVip) {
                            true -> Color.Red
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
            }
        }
    }
}

// Previews

@Preview(
    device = "id:pixel_6",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview() {
    TraktTheme {
        ProfileScreenContent(
            state = ProfileState(
                user = PreviewData.user1,
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
        ProfileScreenContent(
            state = ProfileState(
                user = null,
            ),
        )
    }
}
