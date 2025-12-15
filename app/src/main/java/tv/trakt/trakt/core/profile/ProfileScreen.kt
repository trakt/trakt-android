@file:OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.profile

import android.content.Context
import android.content.Intent
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
import androidx.compose.foundation.lazy.layout.LazyLayoutCacheWindow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import timber.log.Timber
import tv.trakt.trakt.common.Config
import tv.trakt.trakt.common.Config.WEB_V3_BASE_URL
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.ui.theme.colors.Red400
import tv.trakt.trakt.core.profile.sections.favorites.ProfileFavoritesView
import tv.trakt.trakt.core.profile.sections.history.ProfileHistoryView
import tv.trakt.trakt.core.profile.sections.library.ProfileLibraryView
import tv.trakt.trakt.core.profile.sections.social.ProfileSocialView
import tv.trakt.trakt.core.profile.sections.thismonth.ThisMonthCard
import tv.trakt.trakt.core.profile.sections.thismonth.ThisMonthVipCard
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
    onNavigateToLibrary: () -> Unit,
    onNavigateToDiscover: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToHome: () -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current

    val state by viewModel.state.collectAsStateWithLifecycle()

    var confirmLogout by remember { mutableStateOf(false) }

    LaunchedEffect(state.user, state.logoutLoading) {
        if (state.logoutLoading == DONE && state.user == null) {
            onNavigateToHome()
        }
    }

    ProfileScreen(
        state = state,
        onNavigateToShow = onNavigateToShow,
        onNavigateToMovie = onNavigateToMovie,
        onNavigateToEpisode = onNavigateToEpisode,
        onNavigateToHistory = onNavigateToHistory,
        onNavigateToFavorites = onNavigateToFavorites,
        onNavigateToLibrary = onNavigateToLibrary,
        onNavigateToShows = onNavigateToDiscover,
        onNavigateToMovies = onNavigateToDiscover,
        onSettingsClick = onNavigateToSettings,
        onShareClick = {
            shareProfile(
                user = state.user,
                context = context,
            )
        },
        onLogoutClick = {
            confirmLogout = true
        },
        onVipClick = {
            uriHandler.openUri(Config.WEB_VIP_URL)
        },
    )

    ConfirmationSheet(
        active = confirmLogout,
        onYes = {
            confirmLogout = false
            viewModel.logout()
        },
        onNo = {
            confirmLogout = false
        },
        title = stringResource(R.string.button_text_logout),
        message = stringResource(R.string.warning_prompt_log_out),
        yesColor = Red400,
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
    onNavigateToLibrary: () -> Unit = {},
    onNavigateToShows: () -> Unit = {},
    onNavigateToMovies: () -> Unit = {},
    onVipClick: () -> Unit = {},
    onShareClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
) {
    val inspection = LocalInspectionMode.current

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
            translation = listScrollConnection.resultOffset,
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
                    onShareClick = onShareClick,
                    onLogoutClick = onLogoutClick,
                    onSettingsClick = onSettingsClick,
                    modifier = Modifier
                        .padding(bottom = 8.dp),
                )
            }

            if (state.user != null) {
                if (state.user.isAnyVip) {
                    item {
                        ThisMonthCard(
                            user = state.user,
                            stats = state.monthStats,
                            containerImage = state.monthBackgroundUrl,
                            modifier = Modifier
                                .padding(horizontal = TraktTheme.spacing.mainPageHorizontalSpace)
                                .padding(
                                    bottom = when {
                                        state.user.about.isNullOrBlank() -> TraktTheme.spacing.mainSectionVerticalSpace
                                        else -> TraktTheme.spacing.mainSectionVerticalSpace / 1.5F
                                    },
                                ),
                        )
                    }
                } else {
                    item {
                        ThisMonthVipCard(
                            modifier = Modifier
                                .padding(horizontal = TraktTheme.spacing.mainPageHorizontalSpace)
                                .padding(
                                    bottom = when {
                                        state.user.about.isNullOrBlank() -> TraktTheme.spacing.mainSectionVerticalSpace
                                        else -> TraktTheme.spacing.mainSectionVerticalSpace / 1.5F
                                    },
                                )
                                .onClick(
                                    onClick = onVipClick,
                                ),
                        )
                    }
                }

                if (!state.user.about.isNullOrBlank()) {
                    item {
                        Column(
                            verticalArrangement = spacedBy(4.dp),
                            modifier = Modifier
                                .padding(horizontal = TraktTheme.spacing.mainPageHorizontalSpace)
                                .padding(bottom = TraktTheme.spacing.mainSectionVerticalSpace),
                        ) {
                            Text(
                                text = stringResource(R.string.text_about).uppercase(),
                                color = TraktTheme.colors.textSecondary,
                                style = TraktTheme.typography.heading6.copy(
                                    fontWeight = W500,
                                    fontSize = 13.sp,
                                ),
                                maxLines = 1,
                                overflow = Ellipsis,
                            )
                            Text(
                                text = state.user.about ?: "",
                                style = TraktTheme.typography.paragraphSmaller.copy(
                                    fontSize = 13.sp,
                                ),
                                color = TraktTheme.colors.textPrimary,
                                maxLines = 3,
                                overflow = Ellipsis,
                            )
                        }
                    }
                }

                if (!inspection) {
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
                            onShowsClick = onNavigateToShows,
                            onMoviesClick = onNavigateToMovies,
                            modifier = Modifier
                                .padding(bottom = TraktTheme.spacing.mainSectionVerticalSpace),
                        )
                    }

                    item {
                        ProfileLibraryView(
                            headerPadding = sectionPadding,
                            contentPadding = sectionPadding,
                            onShowClick = onNavigateToShow,
                            onMovieClick = onNavigateToMovie,
                            onMoreClick = onNavigateToLibrary,
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
            }
        }
    }
}

@Composable
private fun TitleBar(
    user: User?,
    modifier: Modifier = Modifier,
    onShareClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
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
                    modifier = Modifier
                        .size(34.dp)
                        .graphicsLayer {
                            translationY = 1.dp.toPx()
                        },
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

                val vipPrefix = remember(user) {
                    when (user.isAnyVip) {
                        true -> "VIP • "
                        else -> ""
                    }
                }
                TraktHeader(
                    title = user.displayName,
                    subtitle = "$vipPrefix${user.location}",
                )
            }

            Row(
                verticalAlignment = CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_settings),
                    contentDescription = null,
                    tint = TraktTheme.colors.textPrimary,
                    modifier = Modifier
                        .size(22.dp)
                        .onClick(onClick = onSettingsClick),
                )

                Box {
                    var showMenu by remember { mutableStateOf(false) }

                    Icon(
                        painter = painterResource(R.drawable.ic_more_vertical),
                        contentDescription = null,
                        tint = TraktTheme.colors.textPrimary,
                        modifier = Modifier
                            .size(18.dp)
                            .onClick {
                                showMenu = true
                            },
                    )

                    DropdownMenu(
                        expanded = showMenu,
                        containerColor = TraktTheme.colors.dialogContainer,
                        shape = RoundedCornerShape(16.dp),
                        onDismissRequest = {
                            showMenu = false
                        },
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = stringResource(R.string.button_text_share),
                                    style = TraktTheme.typography.buttonTertiary,
                                    color = TraktTheme.colors.textPrimary,
                                )
                            },
                            onClick = {
                                onShareClick()
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(R.drawable.ic_share),
                                    contentDescription = null,
                                    tint = TraktTheme.colors.textPrimary,
                                    modifier = Modifier.size(22.dp),
                                )
                            },
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = stringResource(R.string.button_text_logout),
                                    style = TraktTheme.typography.buttonTertiary,
                                    color = TraktTheme.colors.textPrimary,
                                )
                            },
                            onClick = {
                                onLogoutClick()
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(R.drawable.ic_logout),
                                    contentDescription = null,
                                    tint = TraktTheme.colors.textPrimary,
                                    modifier = Modifier.size(22.dp),
                                )
                            },
                        )
                    }
                }
            }
        }
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
    val intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, shareText)
        type = "text/plain"
    }

    context.startActivity(Intent.createChooser(intent, user.displayName))
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
        ProfileScreen(
            state = ProfileState(
                user = PreviewData.user1,
            ),
        )
    }
}
