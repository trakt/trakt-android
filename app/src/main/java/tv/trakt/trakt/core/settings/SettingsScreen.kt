@file:OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.settings

import android.Manifest.permission.POST_NOTIFICATIONS
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION_CODES.TIRAMISU
import android.provider.Settings.ACTION_APP_LOCALE_SETTINGS
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarDuration.Short
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType.Companion.Confirm
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight.Companion.W600
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.BuildConfig
import tv.trakt.trakt.LocalSnackbarState
import tv.trakt.trakt.common.Config
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.extensions.DevicePreview
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.ui.composables.FilmProgressIndicator
import tv.trakt.trakt.common.ui.theme.colors.LightColors
import tv.trakt.trakt.common.ui.theme.colors.Purple300
import tv.trakt.trakt.common.ui.theme.colors.Purple500
import tv.trakt.trakt.common.ui.theme.colors.Purple600
import tv.trakt.trakt.common.ui.theme.colors.Red500
import tv.trakt.trakt.core.notifications.model.DeliveryAdjustment
import tv.trakt.trakt.core.settings.features.notifications.AdjustNotificationTimeSheet
import tv.trakt.trakt.core.settings.ui.SettingsSwitchField
import tv.trakt.trakt.core.settings.ui.SettingsTextField
import tv.trakt.trakt.core.settings.ui.SettingsValueField
import tv.trakt.trakt.helpers.SimpleScrollConnection
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.ScrollableBackdropImage
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.components.confirmation.ConfirmationSheet
import tv.trakt.trakt.ui.components.input.SingleInputSheet
import tv.trakt.trakt.ui.components.whatsnew.openPlayStore
import tv.trakt.trakt.ui.theme.DefaultCardShape
import tv.trakt.trakt.ui.theme.TraktTheme
import tv.trakt.trakt.ui.theme.model.ThemeMode
import java.util.Locale

private const val SECTION_SPACING_DP = 12
internal const val SECTION_ITEM_HEIGHT_DP = 32

@Composable
internal fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateHome: () -> Unit,
    onNavigateYounify: () -> Unit,
    onNavigateBlockedUsers: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    val snackbar = LocalSnackbarState.current
    val haptic = LocalHapticFeedback.current
    val resources = LocalResources.current

    val state by viewModel.state.collectAsStateWithLifecycle()

    var confirmLogout by remember { mutableStateOf(false) }

    LaunchedEffect(state.user, state.logoutLoading) {
        if (state.logoutLoading == Done && state.user == null) {
            onNavigateHome()
        }
    }

    LaunchedEffect(state.info) {
        if (state.info != null) {
            haptic.performHapticFeedback(Confirm)
            snackbar.showSnackbar(
                message = resources.getString(R.string.text_info_cover_removed),
                duration = Short,
            )
            viewModel.clearInfo()
        }
    }

    SettingsScreenContent(
        state = state,
        onSetDisplayName = viewModel::updateUserDisplayName,
        onSetLocation = viewModel::updateUserLocation,
        onSetAbout = viewModel::updateUserAbout,
        onEnableMultiplePlays = viewModel::enableMultiplePlays,
        onEnableRatePrompts = viewModel::enableRatePrompts,
        onEnablePrivateAccount = viewModel::enablePrivateAccount,
        onEnableNotifications = viewModel::enableNotifications,
        onSetDeliveryTime = viewModel::setNotificationDeliveryTime,
        onSetThemeMode = viewModel::setThemeMode,
        onClearCoverImage = viewModel::clearCoverImage,
        onYounifyClick = onNavigateYounify,
        onBlockedUsersClick = onNavigateBlockedUsers,
        onGithubClick = {
            uriHandler.openUri(Config.WEB_SOCIAL_GITHUB_URL)
        },
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
        yesColor = Red500,
    )
}

@Composable
private fun SettingsScreenContent(
    state: SettingsState,
    modifier: Modifier = Modifier,
    onSetDisplayName: (String?) -> Unit = { },
    onSetLocation: (String?) -> Unit = { },
    onSetAbout: (String?) -> Unit = { },
    onClearCoverImage: () -> Unit = { },
    onYounifyClick: () -> Unit = { },
    onBlockedUsersClick: () -> Unit = { },
    onEnableMultiplePlays: (Boolean) -> Unit = { },
    onEnableRatePrompts: (Boolean) -> Unit = { },
    onEnablePrivateAccount: (Boolean) -> Unit = { },
    onEnableNotifications: (Boolean) -> Unit = { },
    onSetDeliveryTime: (DeliveryAdjustment) -> Unit = { },
    onSetThemeMode: (ThemeMode) -> Unit = { },
    onLogoutClick: () -> Unit = { },
    onGithubClick: () -> Unit = { },
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
            .plus(TraktTheme.spacing.mainPageBottomSpace),
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
                onGithubClick = onGithubClick,
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
                    onClearCoverImage = onClearCoverImage,
                    onEnablePrivateAccount = onEnablePrivateAccount,
                )

                SettingsTracking(
                    state = state,
                    onEnableMultiplePlays = onEnableMultiplePlays,
                    onEnableRatePrompts = onEnableRatePrompts,
                    onBlockedUsersClick = onBlockedUsersClick,
                )

                SettingsStreaming(
                    state = state,
                    onAutomaticTrackingClick = onYounifyClick,
                )

                SettingsNotifications(
                    state = state,
                    onEnableNotifications = onEnableNotifications,
                    onSetDeliveryTime = onSetDeliveryTime,
                )

                SettingsAppearance(
                    state = state,
                    onSetThemeMode = onSetThemeMode,
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
    onGithubClick: () -> Unit = {},
    onInstagramClick: () -> Unit = {},
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
                painter = painterResource(R.drawable.ic_github),
                contentDescription = null,
                tint = TraktTheme.colors.textPrimary,
                modifier = Modifier
                    .padding(end = 4.dp)
                    .size(22.dp)
                    .onClick(onClick = onGithubClick),
            )

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
    onClearCoverImage: () -> Unit = { },
    onEnablePrivateAccount: (Boolean) -> Unit = { },
) {
    var displayNameSheet by remember { mutableStateOf<String?>(null) }
    var locationSheet by remember { mutableStateOf<String?>(null) }
    var aboutSheet by remember { mutableStateOf<String?>(null) }

    val privateAccount = remember(state.user) {
        state.user?.isPrivate == true
    }

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
                titleColor = getHeaderColor(),
                subtitle = "@${state.user?.username}",
                subtitleColor = TraktTheme.colors.textPrimary,
                modifier = Modifier.padding(bottom = 4.dp),
            )

            if (state.accountLoading.isLoading) {
                FilmProgressIndicator(
                    size = 18.dp,
                )
            }
        }

        SettingsValueField(
            text = stringResource(R.string.text_display_name),
            value = state.user?.name,
            enabled = !state.logoutLoading.isLoading && !state.accountLoading.isLoading,
            onClick = {
                displayNameSheet = state.user?.name
            },
        )

        SettingsValueField(
            text = stringResource(R.string.text_display_email),
            value = state.user?.email,
            enabled = false,
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
            text = stringResource(R.string.text_about),
            value = state.user?.about,
            enabled = !state.logoutLoading.isLoading && !state.accountLoading.isLoading,
            onClick = {
                aboutSheet = state.user?.about
            },
        )

        SettingsSwitchField(
            text = stringResource(R.string.text_private_account),
            checked = privateAccount,
            enabled = !state.logoutLoading.isLoading && !state.accountLoading.isLoading,
            onClick = {
                onEnablePrivateAccount(!privateAccount)
            },
            modifier = Modifier.padding(top = SECTION_SPACING_DP.dp / 1.5F),
        )

        if (!state.user?.settings?.coverImage.isNullOrBlank()) {
            SettingsTextField(
                text = stringResource(R.string.header_settings_clear_cover),
                description = stringResource(R.string.text_settings_clear_cover),
                icon = null,
                enabled = !state.logoutLoading.isLoading && !state.accountLoading.isLoading,
                onClick = onClearCoverImage,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }

    // Sheets

    SingleInputSheet(
        active = displayNameSheet != null,
        title = stringResource(R.string.text_display_name),
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
        title = stringResource(R.string.text_about),
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
private fun SettingsTracking(
    state: SettingsState,
    modifier: Modifier = Modifier,
    onEnableMultiplePlays: (Boolean) -> Unit,
    onEnableRatePrompts: (Boolean) -> Unit = {},
    onBlockedUsersClick: () -> Unit = {},
) {
    val multiplePlays = remember(state.user?.settings) {
        state.user?.settings?.watchOnlyOnce != true
    }

    val ratePrompts = remember(state.user?.settings) {
        state.user?.settings?.ratingPrompts == true
    }

    Column(
        verticalArrangement = spacedBy(SECTION_SPACING_DP.dp),
        modifier = modifier,
    ) {
        TraktHeader(
            title = stringResource(R.string.header_behavior).uppercase(),
            titleColor = getHeaderColor(),
            titleStyle = TraktTheme.typography.heading6,
        )

        SettingsSwitchField(
            text = stringResource(R.string.text_settings_enable_multiple_plays),
            description = stringResource(R.string.text_settings_enable_multiple_plays_description),
            checked = multiplePlays,
            enabled = !state.logoutLoading.isLoading && !state.accountLoading.isLoading,
            onClick = {
                onEnableMultiplePlays(!multiplePlays)
            },
            modifier = Modifier.padding(top = SECTION_SPACING_DP.dp / 1.5F),
        )

        SettingsSwitchField(
            text = stringResource(R.string.text_settings_show_rating_prompt),
            description = stringResource(R.string.text_settings_show_rating_prompt_description),
            checked = ratePrompts,
            enabled = !state.logoutLoading.isLoading && !state.accountLoading.isLoading,
            onClick = {
                onEnableRatePrompts(!ratePrompts)
            },
            modifier = Modifier.padding(top = SECTION_SPACING_DP.dp / 1.5F),
        )

        SettingsTextField(
            text = stringResource(R.string.heading_blocked_users),
            description = stringResource(R.string.description_blocked_users),
            onClick = onBlockedUsersClick,
            modifier = Modifier.padding(top = SECTION_SPACING_DP.dp),
        )
    }
}

@Composable
private fun SettingsStreaming(
    state: SettingsState,
    modifier: Modifier = Modifier,
    onAutomaticTrackingClick: () -> Unit = {},
) {
    Column(
        verticalArrangement = spacedBy(SECTION_SPACING_DP.dp),
        modifier = modifier,
    ) {
        TraktHeader(
            title = stringResource(R.string.text_streaming_sync).uppercase(),
            titleColor = getHeaderColor(),
            titleStyle = TraktTheme.typography.heading6,
        )

        SettingsTextField(
            text = stringResource(R.string.text_automatic_tracking),
            enabled = !state.logoutLoading.isLoading,
            vipLocked = false,
            onClick = onAutomaticTrackingClick,
        )
    }
}

@Composable
private fun SettingsAppearance(
    state: SettingsState,
    modifier: Modifier = Modifier,
    onSetThemeMode: (ThemeMode) -> Unit = { },
) {
    val context = LocalContext.current
    val config = LocalResources.current.configuration
    val uriHandler = LocalUriHandler.current

    val scope = rememberCoroutineScope()

    val appLocale = remember(config) {
        AppCompatDelegate.getApplicationLocales().toLanguageTags()
    }

    val appLocaleDisplay = remember(config, appLocale) {
        Locale.forLanguageTag(appLocale).getDisplayName(Locale.forLanguageTag(appLocale))
            .ifBlank { "System" }
            .replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString()
            }
    }

    val allLocales = remember(config) {
        BuildConfig.SUPPORTED_LOCALES
            .map {
                val locale = Locale.forLanguageTag(it)
                val displayLabel = locale.getDisplayName(Locale.forLanguageTag(it))
                    .replaceFirstChar { char ->
                        if (char.isLowerCase()) char.titlecase(Locale.ROOT) else char.toString()
                    }
                it to displayLabel
            }
    }

    Column(
        verticalArrangement = spacedBy(SECTION_SPACING_DP.dp),
        modifier = modifier,
    ) {
        TraktHeader(
            title = stringResource(R.string.header_appearance).uppercase(),
            titleColor = getHeaderColor(),
            titleStyle = TraktTheme.typography.heading6,
        )

        Box {
            var themeMenuVisible by remember { mutableStateOf(false) }

            SettingsValueField(
                text = stringResource(R.string.text_theme),
                value = stringResource(state.themeMode.displayName()),
                enabled = !state.logoutLoading.isLoading,
                onClick = {
                    themeMenuVisible = true
                },
            )

            Box(
                modifier = Modifier.align(Alignment.BottomEnd),
            ) {
                DropdownMenu(
                    expanded = themeMenuVisible,
                    containerColor = TraktTheme.colors.dialogContainer,
                    shape = RoundedCornerShape(16.dp),
                    onDismissRequest = {
                        themeMenuVisible = false
                    },
                ) {
                    ThemeMode.entries.forEach { mode ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = stringResource(mode.displayName()),
                                    style = TraktTheme.typography.buttonTertiary,
                                    color = TraktTheme.colors.textPrimary,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            },
                            onClick = {
                                themeMenuVisible = false
                                onSetThemeMode(mode)
                            },
                        )
                    }
                }
            }
        }

        Box {
            val menuVisible = remember(config) { mutableStateOf(false) }

            SettingsValueField(
                text = stringResource(R.string.text_language),
                value = appLocaleDisplay,
                enabled = !state.logoutLoading.isLoading,
                onClick = {
                    if (Build.VERSION.SDK_INT >= TIRAMISU) {
                        val intent = Intent(ACTION_APP_LOCALE_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    } else {
                        menuVisible.value = true
                    }
                },
            )

            Box(
                modifier = Modifier.align(Alignment.BottomEnd),
            ) {
                DropdownMenu(
                    expanded = menuVisible.value,
                    containerColor = TraktTheme.colors.dialogContainer,
                    shape = RoundedCornerShape(16.dp),
                    onDismissRequest = {
                        menuVisible.value = false
                    },
                ) {
                    allLocales.forEach { (locale, displayName) ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = displayName,
                                    style = TraktTheme.typography.buttonTertiary,
                                    color = TraktTheme.colors.textPrimary,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            },
                            onClick = {
                                menuVisible.value = false
                                scope.launch(Dispatchers.Main) {
                                    delay(350)
                                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(locale))
                                }
                            },
                        )
                    }
                }
            }
        }

        SettingsTextField(
            text = stringResource(R.string.header_settings_translations),
            description = stringResource(R.string.text_settings_translations),
            icon = R.drawable.ic_translate,
            iconSize = 17.dp,
            onClick = {
                uriHandler.openUri(Config.WEB_TRANSLATE_URL)
            },
        )
    }
}

@Composable
private fun SettingsNotifications(
    state: SettingsState,
    modifier: Modifier = Modifier,
    onEnableNotifications: (Boolean) -> Unit,
    onSetDeliveryTime: (DeliveryAdjustment) -> Unit,
) {
    val context = LocalContext.current

    val hasPermission = remember(context) {
        if (Build.VERSION.SDK_INT >= TIRAMISU) {
            ContextCompat.checkSelfPermission(context, POST_NOTIFICATIONS) == PERMISSION_GRANTED
        } else {
            // On Android 12 and below, notification permission is granted by default
            true
        }
    }

    // Permission launcher for Android 13+
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            onEnableNotifications(isGranted)
        },
    )

    LaunchedEffect(hasPermission) {
        if (!hasPermission) {
            onEnableNotifications(false)
        }
    }

    var adjustTimeSheet by remember { mutableStateOf(false) }

    Column(
        verticalArrangement = spacedBy(SECTION_SPACING_DP.dp),
        modifier = modifier,
    ) {
        Column {
            TraktHeader(
                title = stringResource(R.string.header_settings_notifications).uppercase(),
                titleColor = getHeaderColor(),
                titleStyle = TraktTheme.typography.heading6,
            )
        }

        SettingsSwitchField(
            text = stringResource(R.string.text_settings_enable_notifications),
            checked = state.notifications,
            enabled = !state.logoutLoading.isLoading && !state.accountLoading.isLoading,
            onClick = {
                if (state.notifications) {
                    onEnableNotifications(false)
                } else if (Build.VERSION.SDK_INT >= TIRAMISU) {
                    if (!hasPermission) {
                        permissionLauncher.launch(POST_NOTIFICATIONS)
                    } else {
                        onEnableNotifications(true)
                    }
                } else {
                    onEnableNotifications(true)
                }
            },
        )

        SettingsTextField(
            text = stringResource(R.string.text_settings_adjust_delivery),
            onClick = {
                adjustTimeSheet = true
            },
        )
    }

    // Sheets

    AdjustNotificationTimeSheet(
        active = adjustTimeSheet,
        initial = state.notificationsDelivery ?: DeliveryAdjustment.DISABLED,
        onApply = onSetDeliveryTime,
        onDismiss = {
            adjustTimeSheet = false
        },
    )
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
            titleColor = getHeaderColor(),
            titleStyle = TraktTheme.typography.heading6,
        )

        SettingsTextField(
            text = stringResource(R.string.text_all_settings),
            enabled = !state.logoutLoading.isLoading,
            onClick = {
                uriHandler.openUri(Config.WEB_SETTINGS_URL)
            },
        )

        if (state.user?.isAnyVip == true) {
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
        }

        SettingsTextField(
            text = stringResource(R.string.link_text_forums),
            enabled = !state.logoutLoading.isLoading,
            onClick = {
                uriHandler.openUri(Config.WEB_FORUMS_URL)
            },
        )

        SettingsTextField(
            text = stringResource(R.string.text_settings_play_subscriptions),
            enabled = !state.logoutLoading.isLoading,
            onClick = onSubscriptionsClick,
        )

        if (state.user?.isAnyVip == true) {
            SettingsTextField(
                text = stringResource(R.string.link_text_feedback),
                enabled = !state.logoutLoading.isLoading,
                icon = R.drawable.ic_feedback,
                iconSize = 17.dp,
                onClick = {
                    uriHandler.openUri(Config.WEB_ROADMAP_URL)
                },
            )
        }

        SettingsTextField(
            text = stringResource(R.string.button_text_logout),
            icon = R.drawable.ic_logout,
            iconSize = 17.dp,
            enabled = !state.logoutLoading.isLoading,
            onClick = onLogoutClick,
        )

        RateTraktView(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
        )

        Row(
            horizontalArrangement = spacedBy(8.dp),
            verticalAlignment = CenterVertically,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 12.dp),
        ) {
            Text(
                text = stringResource(R.string.link_text_terms),
                color = TraktTheme.colors.textSecondary,
                style = TraktTheme.typography.paragraph.copy(
                    fontSize = 14.sp,
                ),
                modifier = Modifier.onClick {
                    uriHandler.openUri(Config.WEB_TERMS_URL)
                },
            )

            Text(
                text = "•",
                color = TraktTheme.colors.textSecondary,
                style = TraktTheme.typography.paragraph.copy(
                    fontSize = 14.sp,
                ),
            )

            Text(
                text = stringResource(R.string.link_text_policy),
                color = TraktTheme.colors.textSecondary,
                style = TraktTheme.typography.paragraph.copy(
                    fontSize = 14.sp,
                ),
                modifier = Modifier.onClick {
                    uriHandler.openUri(Config.WEB_PRIVACY_URL)
                },
            )
        }
    }
}

@Composable
private fun RateTraktView(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    Row(
        verticalAlignment = CenterVertically,
        horizontalArrangement = spacedBy(14.dp),
        modifier = modifier
            .onClick {
                openPlayStore(context)
            }
            .shadow(
                elevation = 2.dp,
                shape = DefaultCardShape,
                clip = false,
            )
            .background(
                color = Purple600,
                shape = DefaultCardShape,
            )
            .padding(horizontal = 16.dp, vertical = 18.dp),
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_mood_face),
            contentDescription = null,
            tint = TraktTheme.colors.textPrimaryOnAccent,
            modifier = Modifier.size(26.dp),
        )

        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = stringResource(R.string.header_rate_us_1),
                style = TraktTheme.typography.paragraphSmall.copy(
                    fontWeight = W600,
                ),
                color = TraktTheme.colors.textPrimaryOnAccent,
                textAlign = TextAlign.Start,
            )

            Text(
                text = stringResource(R.string.header_rate_us_2),
                style = TraktTheme.typography.paragraphSmaller,
                color = TraktTheme.colors.textPrimaryOnAccent,
                textAlign = TextAlign.Start,
            )
        }
    }
}

@Composable
private fun getHeaderColor(): Color {
    return when {
        TraktTheme.colors.isLight -> Purple500
        else -> Purple300
    }
}

// Previews

@DevicePreview
@Composable
private fun Preview() {
    TraktTheme {
        SettingsScreenContent(
            state = SettingsState(
                user = PreviewData.user1.copy(
                    settings = User.Settings(
                        watchOnlyOnce = true,
                        ratingPrompts = false,
                        coverImage = null,
                    ),
                ),
            ),
        )
    }
}

@DevicePreview
@Composable
private fun PreviewLight() {
    TraktTheme(
        colors = LightColors,
    ) {
        SettingsScreenContent(
            state = SettingsState(
                user = PreviewData.user1.copy(
                    settings = User.Settings(
                        watchOnlyOnce = true,
                        ratingPrompts = false,
                        coverImage = null,
                    ),
                ),
            ),
        )
    }
}
