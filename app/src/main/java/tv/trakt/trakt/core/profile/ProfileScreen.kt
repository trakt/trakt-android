@file:OptIn(ExperimentalFoundationApi::class)

package tv.trakt.trakt.core.profile

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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight.Companion.W500
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
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
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun ProfileScreen(
    viewModel: ProfileViewModel,
    onNavigateToShow: (TraktId) -> Unit,
    onNavigateToMovie: (TraktId) -> Unit,
    onNavigateToEpisode: (showId: TraktId, episode: Episode) -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToFavorites: () -> Unit,
    onNavigateToDiscover: () -> Unit,
    onNavigateToSettings: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ProfileScreenContent(
        state = state,
        onNavigateToShow = onNavigateToShow,
        onNavigateToMovie = onNavigateToMovie,
        onNavigateToEpisode = onNavigateToEpisode,
        onNavigateToHistory = onNavigateToHistory,
        onNavigateToFavorites = onNavigateToFavorites,
        onNavigateToShows = onNavigateToDiscover,
        onNavigateToMovies = onNavigateToDiscover,
        onSettingsClick = onNavigateToSettings,
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
    onNavigateToShows: () -> Unit = {},
    onNavigateToMovies: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
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
                    onSettingsClick = onSettingsClick,
                    modifier = Modifier
                        .padding(bottom = 8.dp),
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
                            .padding(
                                bottom = when {
                                    state.user.about.isNullOrBlank() -> TraktTheme.spacing.mainSectionVerticalSpace
                                    else -> TraktTheme.spacing.mainSectionVerticalSpace / 1.5F
                                },
                            ),
                    )
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
                                textAlign = TextAlign.Center,
                                overflow = Ellipsis,
                            )
                        }
                    }
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

            Icon(
                painter = painterResource(R.drawable.ic_settings),
                contentDescription = null,
                tint = TraktTheme.colors.textPrimary,
                modifier = Modifier
                    .size(22.dp)
                    .onClick(onClick = onSettingsClick),
            )
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
