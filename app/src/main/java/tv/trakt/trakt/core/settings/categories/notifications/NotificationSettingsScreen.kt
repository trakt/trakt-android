@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.settings.categories.notifications

import android.Manifest.permission.POST_NOTIFICATIONS
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build
import android.os.Build.VERSION_CODES.TIRAMISU
import android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS
import android.provider.Settings.EXTRA_APP_PACKAGE
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.core.notifications.model.DeliveryAdjustment
import tv.trakt.trakt.core.settings.features.notifications.AdjustNotificationTimeSheet
import tv.trakt.trakt.core.settings.ui.SettingsSegmentedGroup
import tv.trakt.trakt.core.settings.ui.SettingsSubscreen
import tv.trakt.trakt.core.settings.ui.SettingsSwitchField
import tv.trakt.trakt.core.settings.ui.SettingsTextField
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.resources.R

@Composable
internal fun NotificationSettingsScreen(
    viewModel: NotificationSettingsViewModel,
    onNavigateBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    NotificationSettingsContent(
        state = state,
        onEnableNotifications = viewModel::enableNotifications,
        onSetDeliveryTime = viewModel::setDeliveryTime,
        onBackClick = onNavigateBack,
    )
}

@Composable
private fun NotificationSettingsContent(
    state: NotificationSettingsState,
    modifier: Modifier = Modifier,
    onEnableNotifications: (Boolean) -> Unit = { },
    onSetDeliveryTime: (DeliveryAdjustment) -> Unit = { },
    onBackClick: () -> Unit = { },
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

    SettingsSubscreen(
        title = stringResource(R.string.header_settings_notifications),
        onBackClick = onBackClick,
        modifier = modifier,
    ) {
        SettingsSegmentedGroup {
            item {
                SettingsSwitchField(
                    text = stringResource(R.string.text_settings_enable_notifications),
                    checked = state.enabled,
                    onClick = {
                        if (state.enabled) {
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
            }

            item {
                SettingsTextField(
                    text = stringResource(R.string.text_settings_adjust_delivery),
                    onClick = {
                        adjustTimeSheet = true
                    },
                )
            }

            item {
                SettingsTextField(
                    text = stringResource(R.string.text_all_settings),
                    onClick = {
                        val intent = Intent(ACTION_APP_NOTIFICATION_SETTINGS).apply {
                            putExtra(EXTRA_APP_PACKAGE, context.packageName)
                        }
                        context.startActivity(intent)
                    },
                )
            }
        }
    }

    AdjustNotificationTimeSheet(
        active = adjustTimeSheet,
        initial = state.delivery ?: DeliveryAdjustment.DISABLED,
        onApply = onSetDeliveryTime,
        onDismiss = {
            adjustTimeSheet = false
        },
    )
}

@PreviewTraktLightDark
@Composable
private fun Preview() {
    TraktThemePreview {
        NotificationSettingsContent(
            state = NotificationSettingsState(
                enabled = true,
            ),
        )
    }
}
