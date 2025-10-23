package tv.trakt.trakt.core.user.features.profile

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarDuration.Short
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import tv.trakt.trakt.LocalBottomBarVisibility
import tv.trakt.trakt.LocalSnackbarState
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.core.auth.ConfigAuth
import tv.trakt.trakt.core.user.features.profile.sections.favorites.ProfileFavoritesView
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.BackdropImage
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.components.buttons.TertiaryButton
import tv.trakt.trakt.ui.components.confirmation.ConfirmationSheet
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun ProfileScreen(
    viewModel: ProfileViewModel,
    onNavigateBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val localContext = LocalContext.current
    val localSnack = LocalSnackbarState.current
    val localBottomBarVisibility = LocalBottomBarVisibility.current

    var confirmLogout by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        localBottomBarVisibility.value = false
    }

    LaunchedEffect(state.user) {
        if (state.loading == DONE && state.user != null) {
            localSnack.showSnackbar(
                message = localContext.getString(R.string.text_info_signed_in),
                duration = Short,
            )
        } else if (state.loading == DONE) {
            localSnack.showSnackbar(
                message = localContext.getString(R.string.text_info_signed_out),
                duration = Short,
            )
        }
    }

    ProfileScreenContent(
        state = state,
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
    onLogoutClick: () -> Unit = {},
    onBackClick: () -> Unit = {},
) {
    val uriHandler = LocalUriHandler.current

    val topInset = WindowInsets.statusBars.asPaddingValues()
        .calculateTopPadding()
        .plus(2.75.dp)

    val sectionPadding = PaddingValues(
        start = TraktTheme.spacing.mainPageHorizontalSpace,
        end = TraktTheme.spacing.mainPageHorizontalSpace,
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TraktTheme.colors.backgroundPrimary),
    ) {
        BackdropImage(
            imageUrl = state.backgroundUrl,
        )

        Column(
            modifier = Modifier
                .padding(
                    top = topInset,
                ),
        ) {
            Row(
                verticalAlignment = CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = TraktTheme.spacing.mainPageHorizontalSpace)
                    .height(TraktTheme.size.titleBarHeight),
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
                        title = state.user?.displayName ?: stringResource(R.string.page_title_profile),
                        subtitle = state.user?.location,
                    )
                }

                if (state.user == null) {
                    TertiaryButton(
                        text = stringResource(R.string.button_text_join_trakt),
                        icon = painterResource(R.drawable.ic_trakt_icon),
                        height = 34.dp,
                        onClick = {
                            uriHandler.openUri(ConfigAuth.authCodeUrl)
                        },
                    )
                } else {
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
                            val borderColor = remember(state.user) {
                                when (state.user.isAnyVip) {
                                    true -> Color.Red
                                    else -> Color.White
                                }
                            }

                            if (state.user.hasAvatar) {
                                AsyncImage(
                                    model = state.user.images?.avatar?.full,
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

            if (state.user != null) {
                if (!state.user.about.isNullOrBlank()) {
                    Text(
                        text = state.user.about ?: "",
                        style = TraktTheme.typography.paragraphSmall,
                        color = TraktTheme.colors.textSecondary,
                        maxLines = 5,
                        textAlign = TextAlign.Center,
                        overflow = Ellipsis,
                        modifier = Modifier
                            .padding(
                                top = 12.dp,
                                start = TraktTheme.spacing.mainPageHorizontalSpace,
                                end = TraktTheme.spacing.mainPageHorizontalSpace,
                            )
                    )
                }

                ProfileFavoritesView(
                    headerPadding = sectionPadding,
                    contentPadding = sectionPadding,
                    onShowsClick = { },
                    onShowClick = { },
                    onMoviesClick = { },
                    onMovieClick = { },
                    onProfileClick = { },
                    onWatchlistClick = { },
                    modifier = Modifier.padding(top = 20.dp)
                )
            }
        }
    }
}

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
