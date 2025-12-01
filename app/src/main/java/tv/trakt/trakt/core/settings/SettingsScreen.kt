@file:OptIn(ExperimentalFoundationApi::class)

package tv.trakt.trakt.core.settings

import android.content.Intent
import android.os.Build
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import timber.log.Timber
import tv.trakt.trakt.BuildConfig
import tv.trakt.trakt.common.Config
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.ui.theme.colors.Red400
import tv.trakt.trakt.helpers.SimpleScrollConnection
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.ScrollableBackdropImage
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.components.buttons.PrimaryButton
import tv.trakt.trakt.ui.components.confirmation.ConfirmationSheet
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateHome: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    var confirmLogout by remember { mutableStateOf(false) }

    LaunchedEffect(state.user) {
        if (state.logoutLoading == DONE && state.user == null) {
            onNavigateHome()
        }
    }

    SettingsScreenContent(
        state = state,
        onLogoutClick = {
            confirmLogout = true
        },
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
        message = stringResource(R.string.warning_prompt_log_out),
        yesColor = Red400,
    )
}

@Composable
private fun SettingsScreenContent(
    state: SettingsState,
    modifier: Modifier = Modifier,
    onLogoutClick: () -> Unit = { },
    onBackClick: () -> Unit = { },
) {
    val contentPadding = PaddingValues(
        start = TraktTheme.spacing.mainPageHorizontalSpace,
        end = TraktTheme.spacing.mainPageHorizontalSpace,
        top = WindowInsets.statusBars.asPaddingValues()
            .calculateTopPadding()
            .plus(4.dp),
        bottom = WindowInsets.navigationBars.asPaddingValues()
            .calculateBottomPadding()
            .plus(TraktTheme.size.navigationBarHeight * 2),
    )

    val scrollConnection = rememberSaveable(saver = SimpleScrollConnection.Saver) {
        SimpleScrollConnection()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TraktTheme.colors.backgroundPrimary)
            .nestedScroll(scrollConnection),
    ) {
        ScrollableBackdropImage(
            translation = scrollConnection.resultOffset,
        )

        Column(
            modifier = Modifier
                .padding(contentPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            TitleBar(
                modifier = Modifier
                    .onClick {
                        onBackClick()
                    },
            )

            Column(
                verticalArrangement = spacedBy(42.dp),
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(top = 24.dp),
            ) {
                SettingsStreaming(
                    state = state,
                    onAutomaticTrackingClick = { },
                )

                SettingsMisc(
                    state = state,
                )
            }
        }

        PrimaryButton(
            text = stringResource(R.string.button_text_logout),
            containerColor = Red400,
            enabled = state.logoutLoading == LoadingState.IDLE,
            onClick = onLogoutClick,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(
                    PaddingValues(
                        start = TraktTheme.spacing.mainPageHorizontalSpace,
                        end = TraktTheme.spacing.mainPageHorizontalSpace,
                        bottom = WindowInsets.navigationBars.asPaddingValues()
                            .calculateBottomPadding()
                            .plus(TraktTheme.size.navigationBarHeight)
                            .plus(16.dp),
                    ),
                ),
        )
    }
}

@Composable
private fun TitleBar(modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = CenterVertically,
            horizontalArrangement = spacedBy(12.dp),
            modifier = Modifier
                .height(TraktTheme.size.titleBarHeight)
                .graphicsLayer {
                    translationX = -2.dp.toPx()
                },
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_back_arrow),
                tint = TraktTheme.colors.textPrimary,
                contentDescription = null,
            )
            TraktHeader(
                title = stringResource(R.string.page_title_settings),
                subtitle = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
            )
        }
    }
}

@Composable
private fun SettingsStreaming(
    state: SettingsState,
    modifier: Modifier = Modifier,
    onAutomaticTrackingClick: () -> Unit = {},
) {
    Column(
        verticalArrangement = spacedBy(22.dp),
        modifier = modifier,
    ) {
        TraktHeader(
            title = stringResource(R.string.header_settings_streaming).uppercase(),
            titleColor = TraktTheme.colors.textPrimary,
            titleStyle = TraktTheme.typography.heading6,
            subtitle = stringResource(R.string.header_settings_streaming_description),
        )

        SettingsTextField(
            text = stringResource(R.string.header_settings_automatic_tracking),
            enabled = !state.logoutLoading.isLoading,
            onClick = onAutomaticTrackingClick,
        )
    }
}

@Composable
private fun SettingsMisc(
    state: SettingsState,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    Column(
        verticalArrangement = spacedBy(22.dp),
        modifier = modifier,
    ) {
        TraktHeader(
            title = stringResource(R.string.header_settings_account_support).uppercase(),
            titleStyle = TraktTheme.typography.heading6,
            subtitle = stringResource(R.string.header_settings_account_support_description),
        )

        SettingsTextField(
            text = stringResource(R.string.header_settings_support_contact),
            enabled = !state.logoutLoading.isLoading,
            onClick = {
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = "mailto:".toUri()
                    putExtra(Intent.EXTRA_EMAIL, arrayOf(Config.WEB_SUPPORT_MAIL))
                    putExtra(Intent.EXTRA_SUBJECT, "Trakt Support (Android ${Build.VERSION.RELEASE})")
                }

                try {
                    context.startActivity(intent)
                } catch (error: Exception) {
                    // No email client installed
                    Timber.w(error, "Unable to start email client")
                }
            },
        )

        SettingsTextField(
            text = stringResource(R.string.header_settings_forums),
            enabled = !state.logoutLoading.isLoading,
            onClick = {
                uriHandler.openUri(Config.WEB_FORUMS_URL)
            },
        )

        SettingsTextField(
            text = stringResource(R.string.header_settings_terms),
            enabled = !state.logoutLoading.isLoading,
            onClick = {
                uriHandler.openUri(Config.WEB_TERMS_URL)
            },
        )

        SettingsTextField(
            text = stringResource(R.string.header_settings_policy),
            enabled = !state.logoutLoading.isLoading,
            onClick = {
                uriHandler.openUri(Config.WEB_PRIVACY_URL)
            },
        )
    }
}

@Composable
fun SettingsTextField(
    text: String,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = { },
) {
    Row(
        verticalAlignment = CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxWidth()
            .onClick(
                onClick = onClick,
                enabled = enabled,
            ),
    ) {
        Text(
            text = text,
            color = TraktTheme.colors.textSecondary,
            style = TraktTheme.typography.paragraph.copy(
                fontSize = 14.sp,
            ),
        )

        Icon(
            painter = painterResource(R.drawable.ic_chevron_right),
            contentDescription = null,
            tint = TraktTheme.colors.textPrimary,
            modifier = Modifier
                .size(20.dp),
        )
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
        SettingsScreenContent(
            state = SettingsState(
                user = PreviewData.user1,
            ),
        )
    }
}
