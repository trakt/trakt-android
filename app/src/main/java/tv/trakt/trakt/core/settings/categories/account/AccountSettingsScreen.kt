@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.settings.categories.account

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarDuration.Short
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType.Companion.Confirm
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import tv.trakt.trakt.LocalSnackbarState
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.core.settings.ui.SettingsSegmentedGroup
import tv.trakt.trakt.core.settings.ui.SettingsSubscreen
import tv.trakt.trakt.core.settings.ui.SettingsSwitchField
import tv.trakt.trakt.core.settings.ui.SettingsTextField
import tv.trakt.trakt.core.settings.ui.SettingsValueField
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.input.SingleInputSheet

@Composable
internal fun AccountSettingsScreen(
    viewModel: AccountSettingsViewModel,
    onNavigateBack: () -> Unit,
) {
    val snackbar = LocalSnackbarState.current
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current

    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.info.collect { info ->
            haptic.performHapticFeedback(Confirm)
            snackbar.showSnackbar(
                message = info.get(context),
                duration = Short,
            )
        }
    }

    AccountSettingsContent(
        state = state,
        onSetDisplayName = viewModel::updateUserDisplayName,
        onSetUsername = viewModel::updateUsername,
        onSetLocation = viewModel::updateUserLocation,
        onSetAbout = viewModel::updateUserAbout,
        onEnablePrivateAccount = viewModel::enablePrivateAccount,
        onClearCoverImage = viewModel::clearCoverImage,
        onBackClick = onNavigateBack,
    )
}

@Composable
private fun AccountSettingsContent(
    state: AccountSettingsState,
    modifier: Modifier = Modifier,
    onSetDisplayName: (String?) -> Unit = { },
    onSetUsername: (String) -> Unit = { },
    onSetLocation: (String?) -> Unit = { },
    onSetAbout: (String?) -> Unit = { },
    onEnablePrivateAccount: (Boolean) -> Unit = { },
    onClearCoverImage: () -> Unit = { },
    onBackClick: () -> Unit = { },
) {
    var displayNameSheet by remember { mutableStateOf<String?>(null) }
    var usernameSheet by remember { mutableStateOf<String?>(null) }
    var locationSheet by remember { mutableStateOf<String?>(null) }
    var aboutSheet by remember { mutableStateOf<String?>(null) }

    val privateAccount = remember(state.user) {
        state.user?.isPrivate == true
    }

    val enabled = !state.loading.isLoading

    SettingsSubscreen(
        title = stringResource(R.string.link_text_account_settings),
        subtitle = state.user?.username?.let { "@$it" },
        loading = state.loading.isLoading,
        onBackClick = onBackClick,
        modifier = modifier,
    ) {
        SettingsSegmentedGroup(
            title = stringResource(R.string.header_account_details),
        ) {
            item {
                SettingsValueField(
                    text = stringResource(R.string.text_display_name),
                    value = state.user?.name,
                    enabled = enabled,
                    onClick = {
                        displayNameSheet = state.user?.name
                    },
                )
            }

            item {
                SettingsValueField(
                    text = stringResource(R.string.text_username),
                    value = state.user?.username,
                    enabled = enabled,
                    onClick = {
                        usernameSheet = state.user?.username.orEmpty()
                    },
                )
            }

            item {
                SettingsValueField(
                    text = stringResource(R.string.text_display_email),
                    value = state.user?.email,
                    enabled = false,
                )
            }

            item {
                SettingsValueField(
                    text = stringResource(R.string.text_location),
                    value = state.user?.location,
                    enabled = enabled,
                    onClick = {
                        locationSheet = state.user?.location
                    },
                )
            }

            item {
                SettingsValueField(
                    text = stringResource(R.string.text_about),
                    value = state.user?.about,
                    enabled = enabled,
                    onClick = {
                        aboutSheet = state.user?.about
                    },
                )
            }
        }

        SettingsSegmentedGroup {
            item {
                SettingsSwitchField(
                    text = stringResource(R.string.text_private_account),
                    checked = privateAccount,
                    enabled = enabled,
                    onClick = {
                        onEnablePrivateAccount(!privateAccount)
                    },
                )
            }

            if (!state.user?.settings?.coverImage.isNullOrBlank()) {
                item {
                    SettingsTextField(
                        text = stringResource(R.string.header_settings_clear_cover),
                        description = stringResource(R.string.text_settings_clear_cover),
                        icon = null,
                        enabled = enabled,
                        onClick = onClearCoverImage,
                    )
                }
            }
        }
    }

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
        active = usernameSheet != null,
        title = stringResource(R.string.text_username),
        description = stringResource(R.string.input_prompt_username),
        initialInput = usernameSheet,
        onApply = {
            val username = it?.trim()
            if (!username.isNullOrBlank() && usernameSheet != username) {
                onSetUsername(username)
            }
        },
        onDismiss = {
            usernameSheet = null
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

@PreviewTraktLightDark
@Composable
private fun Preview() {
    TraktThemePreview {
        AccountSettingsContent(
            state = AccountSettingsState(
                user = PreviewData.user1,
            ),
        )
    }
}
