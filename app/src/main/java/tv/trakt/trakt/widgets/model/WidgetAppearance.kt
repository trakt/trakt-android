package tv.trakt.trakt.widgets.model

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.ui.theme.model.ThemeMode

@Immutable
internal data class WidgetAppearance(
    val background: WidgetBackground = WidgetBackground.Solid,
    val theme: ThemeMode = ThemeMode.Default,
    val titleVisible: Boolean = true,
)
