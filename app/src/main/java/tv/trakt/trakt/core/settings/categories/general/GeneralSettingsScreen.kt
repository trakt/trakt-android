@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.settings.categories.general

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import tv.trakt.trakt.common.Config
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.ui.theme.colors.Red400
import tv.trakt.trakt.common.ui.theme.colors.Red500
import tv.trakt.trakt.core.settings.ui.SettingsImdbLinksField
import tv.trakt.trakt.core.settings.ui.SettingsSegmentedGroup
import tv.trakt.trakt.core.settings.ui.SettingsSubscreen
import tv.trakt.trakt.core.settings.ui.SettingsSwitchField
import tv.trakt.trakt.core.settings.ui.SettingsTextField
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.confirmation.ConfirmationSheet
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun GeneralSettingsScreen(
    viewModel: GeneralSettingsViewModel,
    onNavigateHome: () -> Unit,
    onNavigateBlockedUsers: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    var confirmLogout by remember { mutableStateOf(false) }

    LaunchedEffect(state.user, state.logoutLoading) {
        if (state.logoutLoading == Done && state.user == null) {
            onNavigateHome()
        }
    }

    GeneralSettingsContent(
        state = state,
        onEnableSpoilers = viewModel::enableSpoilers,
        onEnableMultiplePlays = viewModel::enableMultiplePlays,
        onEnableRatePrompts = viewModel::enableRatePrompts,
        onBlockedUsersClick = onNavigateBlockedUsers,
        onSubscriptionsClick = {
            uriHandler.openUri(Config.WEB_GOOGLE_SUBSCRIPTIONS)
        },
        onAllSettingsClick = {
            uriHandler.openUri(Config.WEB_SETTINGS_URL)
        },
        onLogoutClick = {
            confirmLogout = true
        },
        onBackClick = onNavigateBack,
    )

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
private fun GeneralSettingsContent(
    state: GeneralSettingsState,
    modifier: Modifier = Modifier,
    onEnableSpoilers: (Boolean) -> Unit = { },
    onEnableMultiplePlays: (Boolean) -> Unit = { },
    onEnableRatePrompts: (Boolean) -> Unit = { },
    onBlockedUsersClick: () -> Unit = { },
    onSubscriptionsClick: () -> Unit = { },
    onAllSettingsClick: () -> Unit = { },
    onLogoutClick: () -> Unit = { },
    onBackClick: () -> Unit = { },
) {
    val spoilers = remember(state.user?.settings) {
        state.user?.settings?.hideSpoilers != true
    }

    val multiplePlays = remember(state.user?.settings) {
        state.user?.settings?.watchOnlyOnce != true
    }

    val ratePrompts = remember(state.user?.settings) {
        state.user?.settings?.ratingPrompts == true
    }

    val enabled = !state.loading.isLoading && !state.logoutLoading.isLoading

    SettingsSubscreen(
        title = stringResource(R.string.link_text_general_settings),
        loading = state.loading.isLoading,
        onBackClick = onBackClick,
        modifier = modifier,
    ) {
        SettingsSegmentedGroup(
            title = stringResource(R.string.header_behavior),
        ) {
            item {
                SettingsSwitchField(
                    text = stringResource(R.string.text_show_spoilers),
                    description = stringResource(R.string.description_behavior),
                    checked = spoilers,
                    enabled = enabled,
                    onClick = {
                        onEnableSpoilers(!spoilers)
                    },
                )
            }

            item {
                SettingsSwitchField(
                    text = stringResource(R.string.text_settings_enable_multiple_plays),
                    description = stringResource(R.string.text_settings_enable_multiple_plays_description),
                    checked = multiplePlays,
                    enabled = enabled,
                    onClick = {
                        onEnableMultiplePlays(!multiplePlays)
                    },
                )
            }

            item {
                SettingsSwitchField(
                    text = stringResource(R.string.text_settings_show_rating_prompt),
                    description = stringResource(R.string.text_settings_show_rating_prompt_description),
                    checked = ratePrompts,
                    enabled = enabled,
                    onClick = {
                        onEnableRatePrompts(!ratePrompts)
                    },
                )
            }

            item {
                SettingsImdbLinksField()
            }

            item {
                SettingsTextField(
                    text = stringResource(R.string.heading_blocked_users),
                    description = stringResource(R.string.description_blocked_users),
                    onClick = onBlockedUsersClick,
                )
            }
        }

        SettingsSegmentedGroup {
            item {
                SettingsTextField(
                    text = stringResource(R.string.text_settings_play_subscriptions),
                    onClick = onSubscriptionsClick,
                )
            }

            item {
                SettingsTextField(
                    text = stringResource(R.string.text_all_settings),
                    onClick = onAllSettingsClick,
                )
            }
        }

        SettingsSegmentedGroup {
            item {
                SettingsTextField(
                    text = stringResource(R.string.button_text_logout),
                    icon = R.drawable.ic_logout,
                    iconSize = 17.dp,
                    color = when (TraktTheme.colors.isLight) {
                        true -> Red500
                        false -> Red400
                    },
                    enabled = enabled,
                    onClick = onLogoutClick,
                )
            }
        }
    }
}

@PreviewTraktLightDark
@Composable
private fun Preview() {
    TraktThemePreview {
        GeneralSettingsContent(
            state = GeneralSettingsState(
                user = PreviewData.user1,
            ),
        )
    }
}
