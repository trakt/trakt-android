package tv.trakt.trakt.core.profile

import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import tv.trakt.trakt.LocalBottomBarVisibility
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.core.auth.ConfigAuth
import tv.trakt.trakt.helpers.preview.PreviewData
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.BackdropImage
import tv.trakt.trakt.ui.components.buttons.PrimaryButton
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun ProfileScreen(
    viewModel: ProfileViewModel,
    onNavigateBack: () -> Unit,
) {
    val localBottomBarVisibility = LocalBottomBarVisibility.current
    LaunchedEffect(Unit) {
        localBottomBarVisibility.value = false
    }

    val state by viewModel.state.collectAsStateWithLifecycle()

    ProfileScreenContent(
        state = state,
        onLogoutClick = { viewModel.logoutUser() },
        onBackClick = onNavigateBack,
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

    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        BackdropImage(
            imageUrl = state.backgroundUrl,
        )

        Row(
            verticalAlignment = CenterVertically,
            horizontalArrangement = spacedBy(12.dp),
            modifier = Modifier
                .padding(top = topInset)
                .height(56.dp)
                .onClick(onBackClick),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_back_arrow),
                tint = Color.White,
                contentDescription = "Back",
                modifier = Modifier
                    .padding(
                        start = TraktTheme.spacing.mainPageHorizontalSpace - 2.dp,
                    ),
            )
            Text(
                text = stringResource(R.string.header_profile_title),
                color = TraktTheme.colors.textPrimary,
                style = TraktTheme.typography.heading5,
            )
        }

        val headerName = remember(state.profile) {
            state.profile?.name?.ifBlank {
                state.profile.username
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Center),
        ) {
            if (state.isSignedIn) {
                Column(
                    modifier = Modifier
                        .padding(bottom = 16.dp),
                ) {
                    Text(
                        text = stringResource(R.string.header_hello, headerName ?: ""),
                        color = TraktTheme.colors.textPrimary,
                        style = TraktTheme.typography.heading4,
                    )
                    if (!state.profile?.location.isNullOrBlank()) {
                        Text(
                            text = state.profile.location ?: "",
                            color = TraktTheme.colors.textSecondary,
                            style = TraktTheme.typography.heading6,
                        )
                    }
                }
            }

            if (!state.isSignedIn) {
                PrimaryButton(
                    enabled = !state.loading.isLoading,
                    loading = state.loading.isLoading,
                    text = stringResource(R.string.log_in),
                    onClick = {
                        uriHandler.openUri(ConfigAuth.authCodeUrl)
                    },
                )
            } else {
                PrimaryButton(
                    enabled = !state.loading.isLoading,
                    loading = state.loading.isLoading,
                    text = stringResource(R.string.log_out),
                    onClick = onLogoutClick,
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
                isSignedIn = true,
                profile = PreviewData.user1,
            ),
        )
    }
}
