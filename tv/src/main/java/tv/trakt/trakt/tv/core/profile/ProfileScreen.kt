package tv.trakt.trakt.tv.core.profile

import PrimaryButton
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import coil3.ColorImage
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.tv.R
import tv.trakt.trakt.tv.common.model.Images.Size.FULL
import tv.trakt.trakt.tv.core.details.ui.BackdropImage
import tv.trakt.trakt.tv.core.episodes.model.Episode
import tv.trakt.trakt.tv.core.profile.sections.favorites.movies.ProfileFavoriteMoviesView
import tv.trakt.trakt.tv.core.profile.sections.favorites.shows.ProfileFavoriteShowsView
import tv.trakt.trakt.tv.core.profile.sections.history.ProfileHistoryView
import tv.trakt.trakt.tv.helpers.preview.PreviewData
import tv.trakt.trakt.tv.ui.theme.TraktTheme

private val sections = listOf(
    "header",
    "history",
    "favoriteShows",
    "favoriteMovies",
)

@Composable
internal fun ProfileScreen(
    viewModel: ProfileViewModel,
    onNavigateToMovie: (TraktId) -> Unit,
    onNavigateToShow: (TraktId) -> Unit,
    onNavigateToEpisode: (showId: TraktId, episode: Episode) -> Unit,
    onNavigateToHistoryViewAll: () -> Unit,
    onNavigateToFavShowsViewAll: () -> Unit,
    onNavigateToFavMoviesViewAll: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ProfileScreenContent(
        state = state,
        onMovieClick = onNavigateToMovie,
        onShowClick = onNavigateToShow,
        onEpisodeClick = onNavigateToEpisode,
        onHistoryViewAllClick = onNavigateToHistoryViewAll,
        onFavShowsViewAll = onNavigateToFavShowsViewAll,
        onFavMoviesViewAll = onNavigateToFavMoviesViewAll,
        onLogoutClick = {
            viewModel.logout()
        },
    )
}

@Composable
private fun ProfileScreenContent(
    state: ProfileState,
    modifier: Modifier = Modifier,
    onMovieClick: (TraktId) -> Unit,
    onShowClick: (TraktId) -> Unit,
    onEpisodeClick: (showId: TraktId, episode: Episode) -> Unit,
    onHistoryViewAllClick: () -> Unit,
    onFavShowsViewAll: () -> Unit,
    onFavMoviesViewAll: () -> Unit,
    onLogoutClick: () -> Unit,
) {
    var focusedSection by rememberSaveable { mutableStateOf<String?>(null) }
    var focusedImageUrl by remember { mutableStateOf<String?>(null) }

    val focusRequesters = remember {
        sections.associateBy(
            keySelector = { it },
            valueTransform = { FocusRequester() },
        )
    }

    LaunchedEffect(Unit) {
        runCatching {
            focusRequesters["header"]?.requestFocus()
        }
    }

    Box(
        contentAlignment = Alignment.TopStart,
        modifier = modifier
            .fillMaxSize()
            .background(TraktTheme.colors.backgroundPrimary)
            .focusProperties {
                onEnter = {
                    focusRequesters[focusedSection]?.requestFocus()
                }
            },
    ) {
        BackdropImage(
            imageUrl = focusedImageUrl ?: state.backgroundUrl,
            saturation = 0F,
            crossfade = true,
        )

        val sectionPadding = PaddingValues(
            start = TraktTheme.spacing.mainContentStartSpace,
            end = TraktTheme.spacing.mainContentEndSpace,
        )

        LazyColumn(
            verticalArrangement = spacedBy(TraktTheme.spacing.mainRowVerticalSpace),
            contentPadding = PaddingValues(
                vertical = TraktTheme.spacing.mainContentVerticalSpace + 8.dp,
            ),
            modifier = Modifier
                .focusRestorer(),
        ) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(sectionPadding)
                        .focusGroup(),
                ) {
                    ProfileHeader(
                        state = state,
                        modifier = Modifier
                            .focusRequester(focusRequesters.getValue("header"))
                            .focusable(),
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    PrimaryButton(
                        text = stringResource(R.string.log_out),
                        onClick = onLogoutClick,
                        modifier = Modifier.widthIn(max = 128.dp),
                    )
                }
            }

            item {
                ProfileHistoryView(
                    headerPadding = sectionPadding,
                    contentPadding = sectionPadding,
                    onMovieClick = onMovieClick,
                    onEpisodeClick = onEpisodeClick,
                    onViewAllClick = onHistoryViewAllClick,
                    onLoaded = {
                        focusRequesters
                            .getValue("history")
                            .requestFocus()
                    },
                    onFocused = { item ->
                        focusedSection = "history"
                        focusedImageUrl = item?.backdropImageUrl
                    },
                    modifier = Modifier
                        .focusRequester(focusRequesters.getValue("history")),
                )
            }

            item {
                ProfileFavoriteShowsView(
                    headerPadding = sectionPadding,
                    contentPadding = sectionPadding,
                    onFocused = { item ->
                        focusedSection = "favoriteShows"
                        focusedImageUrl = item?.images?.getFanartUrl(FULL)
                    },
                    onShowClick = onShowClick,
                    onViewAllClick = onFavShowsViewAll,
                    modifier = Modifier
                        .focusRequester(focusRequesters.getValue("favoriteShows")),
                )
            }

            item {
                ProfileFavoriteMoviesView(
                    headerPadding = sectionPadding,
                    contentPadding = sectionPadding,
                    onFocused = { item ->
                        focusedSection = "favoriteMovies"
                        focusedImageUrl = item?.images?.getFanartUrl(FULL)
                    },
                    onMovieClick = onMovieClick,
                    onViewAllClick = onFavMoviesViewAll,
                    modifier = Modifier
                        .focusRequester(focusRequesters.getValue("favoriteMovies")),
                )
            }
        }
    }
}

@Composable
private fun ProfileHeader(
    state: ProfileState,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        val headerName = remember(state.user) {
            state.user?.name?.ifBlank {
                state.user.username
            }
        }

        ProfileAvatar(
            profile = state.user,
        )

        Column {
            Text(
                text = stringResource(R.string.header_profile, headerName ?: ""),
                color = TraktTheme.colors.textPrimary,
                style = TraktTheme.typography.heading4,
            )
            if (!state.user?.location.isNullOrBlank()) {
                Text(
                    text = state.user.location!!,
                    color = TraktTheme.colors.textSecondary,
                    style = TraktTheme.typography.heading6,
                )
            }
        }
    }
}

@Composable
private fun ProfileAvatar(
    profile: User?,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(44.dp),
    ) {
        if (profile?.hasImage == true) {
            AsyncImage(
                model = profile.images?.avatar?.full,
                contentDescription = "User avatar",
                contentScale = ContentScale.Crop,
                error = painterResource(R.drawable.ic_person_placeholder),
                modifier = Modifier
                    .fillMaxSize()
                    .border(2.dp, Color.White, CircleShape)
                    .clip(CircleShape),
            )
        } else {
            Image(
                painter = painterResource(R.drawable.ic_person_placeholder),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .border(2.dp, Color.White, CircleShape)
                    .clip(CircleShape),
            )
        }

        if (profile?.isAnyVip == true) {
            Icon(
                painter = painterResource(R.drawable.ic_crown),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .graphicsLayer {
                        val offset = 5.dp
                        translationX = offset.toPx()
                        translationY = -offset.toPx()
                    }
                    .shadow(
                        elevation = 1.dp,
                        shape = CircleShape,
                    )
                    .background(Color.Red, shape = CircleShape)
                    .size(20.dp)
                    .padding(bottom = (3.5).dp, top = 3.dp),
            )
        }
    }
}

@OptIn(ExperimentalCoilApi::class)
@Preview(
    device = "id:tv_4k",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview() {
    TraktTheme {
        val previewHandler = AsyncImagePreviewHandler {
            ColorImage(Color.LightGray.toArgb())
        }
        CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
            ProfileScreenContent(
                state = ProfileState(PreviewData.user1),
                onMovieClick = {},
                onShowClick = {},
                onEpisodeClick = { _, _ -> },
                onHistoryViewAllClick = {},
                onFavShowsViewAll = {},
                onFavMoviesViewAll = {},
                onLogoutClick = {},
            )
        }
    }
}
