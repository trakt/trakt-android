package tv.trakt.trakt.widgets.configuration

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.ui.theme.model.ThemeMode
import tv.trakt.trakt.widgets.model.WidgetAppearance
import tv.trakt.trakt.widgets.model.WidgetBackground

@Immutable
internal data class WidgetConfigurationState(
    val background: WidgetBackground = WidgetAppearance().background,
    val theme: ThemeMode = WidgetAppearance().theme,
    val titleVisible: Boolean = WidgetAppearance().titleVisible,
    val loading: LoadingState = Loading,
)
