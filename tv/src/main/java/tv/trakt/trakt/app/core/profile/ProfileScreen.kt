package tv.trakt.trakt.app.core.profile

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
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Text
import coil3.ColorImage
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import kotlinx.coroutines.delay
import tv.trakt.trakt.app.BuildConfig
import tv.trakt.trakt.app.common.ui.buttons.PrimaryButton
import tv.trakt.trakt.app.core.details.ui.BackdropImage
import tv.trakt.trakt.app.core.profile.sections.favorites.ProfileFavoritesView
import tv.trakt.trakt.app.core.profile.sections.history.ProfileHistoryView
import tv.trakt.trakt.app.core.profile.sections.library.ProfileLibraryView
import tv.trakt.trakt.app.core.profile.sections.library.model.LibraryItem
import tv.trakt.trakt.app.helpers.extensions.requestSafeFocus
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Images.Size.FULL
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.resources.R
import kotlin.time.Duration.Companion.milliseconds

private val sections = listOf(
    "header",
    "favorites",
    "history",
    "library",
)

@Composable
internal fun ProfileScreen(
    viewModel: ProfileViewModel,
    onNavigateToMovie: (TraktId) -> Unit,
    onNavigateToShow: (TraktId) -> Unit,
    onNavigateToEpisode: (showId: TraktId, episode: Episode) -> Unit,
    onNavigateToHistoryViewAll: () -> Unit,
    onNavigateToFavoritesViewAll: () -> Unit,
    onNavigateToLibraryViewAll: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ProfileScreenContent(
        state = state,
        onMovieClick = onNavigateToMovie,
        onShowClick = onNavigateToShow,
        onEpisodeClick = onNavigateToEpisode,
        onHistoryViewAllClick = onNavigateToHistoryViewAll,
        onFavoritesViewAll = onNavigateToFavoritesViewAll,
        onLibraryViewAll = onNavigateToLibraryViewAll,
        onLogoutClick = {
            viewModel.logout()
        },
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun ProfileScreenContent(
    state: ProfileState,
    modifier: Modifier = Modifier,
    onMovieClick: (TraktId) -> Unit,
    onShowClick: (TraktId) -> Unit,
    onEpisodeClick: (showId: TraktId, episode: Episode) -> Unit,
    onHistoryViewAllClick: () -> Unit,
    onFavoritesViewAll: () -> Unit,
    onLibraryViewAll: () -> Unit,
    onLogoutClick: () -> Unit,
) {
    var focusedInitial by rememberSaveable { mutableStateOf(false) }
    var focusedSection by rememberSaveable { mutableStateOf<String?>(null) }
    var focusedImageUrl by remember { mutableStateOf<String?>(null) }

    val focusRequesters = remember {
        sections.associateBy(
            keySelector = { it },
            valueTransform = { FocusRequester() },
        )
    }

    // When a section swaps its loading skeletons for real content, the focused
    // skeleton is disposed and focus would be lost. Re-request focus on the
    // section the user was on.
    val refocusOnLoad: (String) -> Unit = { section ->
        if (focusedSection == section) {
            focusRequesters[section]?.requestSafeFocus()
        }
    }

    LaunchedEffect(Unit) {
        if (focusedSection == null) {
            focusedSection = "header"
            focusRequesters["header"]?.requestSafeFocus()
        } else {
            delay(500.milliseconds)
            focusRequesters[focusedSection]?.requestSafeFocus()
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
                .focusRestorer()
                .focusProperties {
                    // Prevent focus escaping the list vertically (e.g. to the side
                    // menu) when a fast D-pad scroll targets a not-yet-focusable item.
                    exit = { direction ->
                        when (direction) {
                            FocusDirection.Down, FocusDirection.Up -> FocusRequester.Cancel
                            else -> FocusRequester.Default
                        }
                    }
                }
                .focusGroup(),
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
                            .onFocusChanged {
                                if (it.isFocused) {
                                    focusedSection = "header"
                                    focusedImageUrl = null
                                }
                            }
                            .focusable(),
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Column(
                        verticalArrangement = spacedBy(4.dp),
                        horizontalAlignment = Alignment.End,
                    ) {
                        PrimaryButton(
                            text = stringResource(R.string.button_text_logout),
                            onClick = onLogoutClick,
                            modifier = Modifier.widthIn(max = 164.dp),
                        )
                        Text(
                            text = "v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                            color = TraktTheme.colors.textSecondary,
                            style = TraktTheme.typography.heading6,
                        )
                    }
                }
            }

            item {
                ProfileFavoritesView(
                    headerPadding = sectionPadding,
                    contentPadding = sectionPadding,
                    onLoaded = {
                        if (!focusedInitial) {
                            focusedInitial = true
                            focusedSection = "favorites"
                            focusRequesters["favorites"]?.requestSafeFocus()
                        } else {
                            refocusOnLoad("favorites")
                        }
                    },
                    onFocused = { item ->
                        focusedSection = "favorites"
                        focusedImageUrl = item?.fullFanartImage
                    },
                    onShowClick = onShowClick,
                    onMovieClick = onMovieClick,
                    onViewAllClick = onFavoritesViewAll,
                    modifier = Modifier
                        .focusRequester(focusRequesters.getValue("favorites")),
                )
            }

            item {
                ProfileHistoryView(
                    headerPadding = sectionPadding,
                    contentPadding = sectionPadding,
                    onMovieClick = onMovieClick,
                    onEpisodeClick = onEpisodeClick,
                    onViewAllClick = onHistoryViewAllClick,
                    onFocused = { item ->
                        focusedSection = "history"
                        focusedImageUrl = item?.backdropImageUrl
                    },
                    onLoaded = { refocusOnLoad("history") },
                    modifier = Modifier
                        .focusRequester(focusRequesters.getValue("history")),
                )
            }

            item {
                ProfileLibraryView(
                    headerPadding = sectionPadding,
                    contentPadding = sectionPadding,
                    onFocused = { item ->
                        focusedSection = "library"
                        focusedImageUrl = when (item) {
                            is LibraryItem.MovieItem -> item.movie.images?.getFanartUrl(FULL)
                            is LibraryItem.EpisodeItem -> item.show.images?.getFanartUrl(FULL)
                            null -> null
                        }
                    },
                    onLoaded = { refocusOnLoad("library") },
                    onMovieClick = onMovieClick,
                    onEpisodeClick = onEpisodeClick,
                    onViewAllClick = onLibraryViewAll,
                    modifier = Modifier
                        .focusRequester(focusRequesters.getValue("library")),
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
        ProfileAvatar(
            profile = state.user,
        )

        Column {
            Text(
                text = state.user?.displayName.orEmpty(),
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
        val vipColor = TraktTheme.colors.vipAccent
        val borderColor = remember(profile?.isAnyVip) {
            if (profile?.isAnyVip == true) vipColor else Color.Transparent
        }
        if (profile?.hasAvatar == true) {
            AsyncImage(
                model = profile.images?.avatar?.full,
                contentDescription = "User avatar",
                contentScale = ContentScale.Crop,
                error = painterResource(R.drawable.ic_person_placeholder),
                modifier = Modifier
                    .fillMaxSize()
                    .border((2.5).dp, borderColor, CircleShape)
                    .clip(CircleShape),
            )
        } else {
            Image(
                painter = painterResource(R.drawable.ic_person_placeholder),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .border((2.5).dp, borderColor, CircleShape)
                    .clip(CircleShape),
            )
        }
    }
}

@OptIn(ExperimentalCoilApi::class)
@Preview(
    device = "id:tv_4k",
    showBackground = true,
    backgroundColor = 0xFF131517,
    locale = "us",
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
                onFavoritesViewAll = {},
                onLibraryViewAll = {},
                onLogoutClick = {},
            )
        }
    }
}
