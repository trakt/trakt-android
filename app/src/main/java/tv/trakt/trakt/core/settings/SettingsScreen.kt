@file:OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)

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
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import timber.log.Timber
import tv.trakt.trakt.BuildConfig
import tv.trakt.trakt.common.Config
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.extensions.uppercaseWords
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.ui.composables.FilmProgressIndicator
import tv.trakt.trakt.common.ui.theme.colors.Red400
import tv.trakt.trakt.helpers.SimpleScrollConnection
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.ScrollableBackdropImage
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.components.confirmation.ConfirmationSheet
import tv.trakt.trakt.ui.components.input.SingleInputSheet
import tv.trakt.trakt.ui.components.vip.VipChip
import tv.trakt.trakt.ui.theme.TraktTheme

private const val SECTION_SPACING_DP = 12
private const val SECTION_ITEM_HEIGHT_DP = 32

@Composable
internal fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateHome: () -> Unit,
    onNavigateYounify: () -> Unit,
    onNavigateVip: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    val uriHandler = LocalUriHandler.current

    val state by viewModel.state.collectAsStateWithLifecycle()

    var confirmLogout by remember { mutableStateOf(false) }

    LaunchedEffect(state.user, state.logoutLoading) {
        if (state.logoutLoading == DONE && state.user == null) {
            onNavigateHome()
        }
    }

    SettingsScreenContent(
        state = state,
        onSetDisplayName = viewModel::updateUserDisplayName,
        onSetLocation = viewModel::updateUserLocation,
        onSetAbout = viewModel::updateUserAbout,
        onYounifyClick = onNavigateYounify,
        onVipClick = onNavigateVip,
        onInstagramClick = {
            uriHandler.openUri(Config.WEB_SOCIAL_INSTAGRAM_URL)
        },
        onTwitterClick = {
            uriHandler.openUri(Config.WEB_SOCIAL_X_URL)
        },
        onLogoutClick = {
            confirmLogout = true
        },
        onSubscriptionsClick = {
            uriHandler.openUri(Config.WEB_GOOGLE_SUBSCRIPTIONS)
        },
        onBackClick = onNavigateBack,
    )

    @OptIn(ExperimentalMaterial3Api::class)
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
private fun SettingsScreenContent(
    state: SettingsState,
    modifier: Modifier = Modifier,
    onSetDisplayName: (String?) -> Unit = { },
    onSetLocation: (String?) -> Unit = { },
    onSetAbout: (String?) -> Unit = { },
    onYounifyClick: () -> Unit = { },
    onLogoutClick: () -> Unit = { },
    onVipClick: () -> Unit = { },
    onInstagramClick: () -> Unit = { },
    onTwitterClick: () -> Unit = { },
    onSubscriptionsClick: () -> Unit = { },
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
            .plus(TraktTheme.size.navigationBarHeight)
            .plus(32.dp),
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
                .verticalScroll(
                    state = rememberScrollState(),
                    overscrollEffect = null,
                )
                .padding(contentPadding),
        ) {
            TitleBar(
                onInstagramClick = onInstagramClick,
                onTwitterClick = onTwitterClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .onClick {
                        onBackClick()
                    },
            )

            Column(
                verticalArrangement = spacedBy(36.dp),
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(top = 20.dp),
            ) {
                SettingsAccount(
                    state = state,
                    onSetDisplayName = onSetDisplayName,
                    onSetLocation = onSetLocation,
                    onSetAbout = onSetAbout,
                )

                SettingsStreaming(
                    state = state,
                    onAutomaticTrackingClick = onYounifyClick,
                    onVipClick = onVipClick,
                )

                SettingsMisc(
                    state = state,
                    onSubscriptionsClick = onSubscriptionsClick,
                    onLogoutClick = onLogoutClick,
                )
            }
        }
    }
}

@Composable
private fun TitleBar(
    modifier: Modifier = Modifier,
    onInstagramClick: () -> Unit = { },
    onTwitterClick: () -> Unit = {},
) {
    Row(
        verticalAlignment = CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier,
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

        Row(
            verticalAlignment = CenterVertically,
            horizontalArrangement = spacedBy(14.dp),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_instagram),
                contentDescription = null,
                tint = TraktTheme.colors.textPrimary,
                modifier = Modifier
                    .size(28.dp)
                    .onClick(onClick = onInstagramClick),
            )

            Icon(
                painter = painterResource(R.drawable.ic_x_twitter),
                contentDescription = null,
                tint = TraktTheme.colors.textPrimary,
                modifier = Modifier
                    .size(28.dp)
                    .onClick(onClick = onTwitterClick),
            )
        }
    }
}

@Composable
private fun SettingsAccount(
    state: SettingsState,
    modifier: Modifier = Modifier,
    onSetDisplayName: (String?) -> Unit = { },
    onSetLocation: (String?) -> Unit = { },
    onSetAbout: (String?) -> Unit = { },
) {
    val uriHandler = LocalUriHandler.current

    var displayNameSheet by remember { mutableStateOf<String?>(null) }
    var locationSheet by remember { mutableStateOf<String?>(null) }
    var aboutSheet by remember { mutableStateOf<String?>(null) }

    Column(
        verticalArrangement = spacedBy(SECTION_SPACING_DP.dp),
        modifier = modifier,
    ) {
        Row(
            verticalAlignment = CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            TraktHeader(
                title = stringResource(R.string.header_account_details).uppercase(),
                titleStyle = TraktTheme.typography.heading6,
                subtitle = "@${state.user?.username}",
                modifier = Modifier.padding(bottom = 4.dp),
            )

            if (state.accountLoading.isLoading) {
                FilmProgressIndicator(
                    size = 18.dp,
                )
            }
        }

        SettingsValueField(
            text = stringResource(R.string.text_display_name).uppercaseWords(),
            value = state.user?.name,
            enabled = !state.logoutLoading.isLoading && !state.accountLoading.isLoading,
            onClick = {
                displayNameSheet = state.user?.name
            },
        )

        SettingsValueField(
            text = stringResource(R.string.text_location),
            value = state.user?.location,
            enabled = !state.logoutLoading.isLoading && !state.accountLoading.isLoading,
            onClick = {
                locationSheet = state.user?.location
            },
        )

        SettingsValueField(
            text = stringResource(R.string.text_about).uppercaseWords(),
            value = state.user?.about,
            enabled = !state.logoutLoading.isLoading && !state.accountLoading.isLoading,
            onClick = {
                aboutSheet = state.user?.about
            },
        )
    }

    // Sheets

    SingleInputSheet(
        active = displayNameSheet != null,
        title = stringResource(R.string.text_display_name).uppercaseWords(),
        description = stringResource(R.string.input_prompt_display_name),
        initialInput = displayNameSheet,
        nullable = true,
        onApply = {
            if (displayNameSheet != it) {
                onSetDisplayName(it)
            }
        },
        onDismiss = {
            displayNameSheet = null
        },
    )

    SingleInputSheet(
        active = locationSheet != null,
        title = stringResource(R.string.text_location),
        description = stringResource(R.string.input_prompt_location),
        initialInput = locationSheet,
        nullable = true,
        onApply = {
            if (locationSheet != it) {
                onSetLocation(it)
            }
        },
        onDismiss = {
            locationSheet = null
        },
    )

    SingleInputSheet(
        active = aboutSheet != null,
        title = stringResource(R.string.text_about).uppercaseWords(),
        description = stringResource(R.string.input_prompt_about),
        initialInput = aboutSheet,
        nullable = true,
        multiline = true,
        onApply = {
            if (aboutSheet != it) {
                onSetAbout(it)
            }
        },
        onDismiss = {
            aboutSheet = null
        },
    )
}

@Composable
private fun SettingsStreaming(
    state: SettingsState,
    modifier: Modifier = Modifier,
    onAutomaticTrackingClick: () -> Unit = {},
    onVipClick: () -> Unit = {},
) {
    val isVip = remember(state.user) {
        state.user?.isAnyVip == true
    }

    Column(
        verticalArrangement = spacedBy(SECTION_SPACING_DP.dp),
        modifier = modifier,
    ) {
        TraktHeader(
            title = stringResource(R.string.text_streaming_sync).uppercase(),
            titleColor = TraktTheme.colors.textPrimary,
            titleStyle = TraktTheme.typography.heading6,
        )

        SettingsTextField(
            text = stringResource(R.string.text_automatic_tracking),
            enabled = !state.logoutLoading.isLoading && isVip,
            vipLocked = state.user != null && !isVip,
            onClick = onAutomaticTrackingClick,
            onVipClick = onVipClick,
        )
    }
}

@Composable
private fun SettingsMisc(
    state: SettingsState,
    modifier: Modifier = Modifier,
    onSubscriptionsClick: () -> Unit = { },
    onLogoutClick: () -> Unit = { },
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    Column(
        verticalArrangement = spacedBy(SECTION_SPACING_DP.dp),
        modifier = modifier,
    ) {
        TraktHeader(
            title = stringResource(R.string.link_text_general_settings).uppercase(),
            titleStyle = TraktTheme.typography.heading6,
        )

        SettingsTextField(
            text = stringResource(R.string.text_all_settings),
            enabled = !state.logoutLoading.isLoading,
            onClick = {
                uriHandler.openUri(Config.WEB_SETTINGS_URL)
            },
        )

        SettingsTextField(
            text = stringResource(R.string.link_text_support),
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
            text = stringResource(R.string.link_text_forums),
            enabled = !state.logoutLoading.isLoading,
            onClick = {
                uriHandler.openUri(Config.WEB_FORUMS_URL)
            },
        )

        SettingsTextField(
            text = stringResource(R.string.link_text_terms),
            enabled = !state.logoutLoading.isLoading,
            onClick = {
                uriHandler.openUri(Config.WEB_TERMS_URL)
            },
        )

        SettingsTextField(
            text = stringResource(R.string.link_text_policy),
            enabled = !state.logoutLoading.isLoading,
            onClick = {
                uriHandler.openUri(Config.WEB_PRIVACY_URL)
            },
        )

        SettingsTextField(
            text = "Google Play Subscriptions",
            enabled = !state.logoutLoading.isLoading,
            onClick = onSubscriptionsClick,
        )

        SettingsTextField(
            text = stringResource(R.string.button_text_logout),
            icon = R.drawable.ic_logout,
            iconSize = 17.dp,
            enabled = !state.logoutLoading.isLoading,
            onClick = onLogoutClick,
        )
    }
}

@Composable
fun SettingsTextField(
    text: String,
    modifier: Modifier = Modifier,
    icon: Int? = R.drawable.ic_chevron_right,
    iconSize: Dp = 20.dp,
    enabled: Boolean = true,
    vipLocked: Boolean = false,
    onClick: () -> Unit = { },
    onVipClick: () -> Unit = { },
) {
    Row(
        verticalAlignment = CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(SECTION_ITEM_HEIGHT_DP.dp)
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

        if (!vipLocked && icon != null) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = TraktTheme.colors.textPrimary,
                modifier = Modifier
                    .size(iconSize),
            )
        } else if (vipLocked) {
            VipChip(
                onClick = onVipClick,
            )
        }
    }
}

@Composable
fun SettingsValueField(
    text: String,
    value: String?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit = { },
) {
    Row(
        verticalAlignment = CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(SECTION_ITEM_HEIGHT_DP.dp)
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

        if (value.isNullOrBlank()) {
            Icon(
                painter = painterResource(R.drawable.ic_chevron_right),
                contentDescription = null,
                tint = TraktTheme.colors.textPrimary,
                modifier = Modifier
                    .size(20.dp),
            )
        } else {
            Text(
                text = value,
                color = TraktTheme.colors.textPrimary,
                style = TraktTheme.typography.paragraph.copy(
                    fontSize = 14.sp,
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.End,
                modifier = Modifier
                    .padding(
                        start = 32.dp,
                        end = 4.dp,
                    ),
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
        SettingsScreenContent(
            state = SettingsState(
                user = PreviewData.user1,
            ),
        )
    }
}
