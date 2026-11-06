package tv.trakt.trakt.core.settings.categories.appearance

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.ui.theme.model.ThemeMode

@Immutable
internal data class AppearanceSettingsState(
    val themeMode: ThemeMode = ThemeMode.Default,
)
