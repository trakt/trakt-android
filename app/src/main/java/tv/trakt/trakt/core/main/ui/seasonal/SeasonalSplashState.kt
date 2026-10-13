package tv.trakt.trakt.core.main.ui.seasonal

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.ui.theme.model.SeasonalTheme

@Immutable
internal data class SeasonalSplashState(
    val season: SeasonalTheme? = null,
)
