package tv.trakt.trakt.core.settings.categories.appearance

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION_CODES.TIRAMISU
import android.provider.Settings.ACTION_APP_LOCALE_SETTINGS
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tv.trakt.trakt.BuildConfig
import tv.trakt.trakt.common.Config
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.core.settings.ui.SettingsSegmentedGroup
import tv.trakt.trakt.core.settings.ui.SettingsSubscreen
import tv.trakt.trakt.core.settings.ui.SettingsTextField
import tv.trakt.trakt.core.settings.ui.SettingsValueField
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme
import tv.trakt.trakt.ui.theme.model.ThemeMode
import java.util.Locale
import kotlin.time.Duration.Companion.milliseconds

@Composable
internal fun AppearanceSettingsScreen(
    viewModel: AppearanceSettingsViewModel,
    onNavigateBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    AppearanceSettingsContent(
        state = state,
        onSetThemeMode = viewModel::setThemeMode,
        onBackClick = onNavigateBack,
    )
}

@Composable
private fun AppearanceSettingsContent(
    state: AppearanceSettingsState,
    modifier: Modifier = Modifier,
    onSetThemeMode: (ThemeMode) -> Unit = { },
    onBackClick: () -> Unit = { },
) {
    val config = LocalResources.current.configuration
    val uriHandler = LocalUriHandler.current

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

    SettingsSubscreen(
        title = stringResource(R.string.header_appearance),
        onBackClick = onBackClick,
        modifier = modifier,
    ) {
        SettingsSegmentedGroup {
            item { ThemeField(state = state, onSetThemeMode = onSetThemeMode) }
            item { LanguageField(appLocaleDisplay = appLocaleDisplay, allLocales = allLocales) }
            item {
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
    }
}

@Composable
private fun ThemeField(
    state: AppearanceSettingsState,
    onSetThemeMode: (ThemeMode) -> Unit,
) {
    Box {
        var themeMenuVisible by remember { mutableStateOf(false) }

        SettingsValueField(
            text = stringResource(R.string.text_theme),
            value = stringResource(state.themeMode.displayName()),
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
}

@Composable
private fun LanguageField(
    appLocaleDisplay: String,
    allLocales: List<Pair<String, String>>,
) {
    val context = LocalContext.current
    val config = LocalResources.current.configuration
    val scope = rememberCoroutineScope()

    Box {
        val menuVisible = remember(config) { mutableStateOf(false) }

        SettingsValueField(
            text = stringResource(R.string.text_language),
            value = appLocaleDisplay,
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
                                delay(350.milliseconds)
                                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(locale))
                            }
                        },
                    )
                }
            }
        }
    }
}

@PreviewTraktLightDark
@Composable
private fun Preview() {
    TraktThemePreview {
        AppearanceSettingsContent(
            state = AppearanceSettingsState(),
        )
    }
}
