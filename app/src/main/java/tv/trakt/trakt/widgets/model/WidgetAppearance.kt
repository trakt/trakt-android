package tv.trakt.trakt.widgets.model

import androidx.compose.runtime.Immutable

/** Look of one placed widget, stored in that widget's Glance state. */
@Immutable
internal data class WidgetAppearance(
    val background: WidgetBackground = WidgetBackground.Solid,
    val titleVisible: Boolean = true,
)
