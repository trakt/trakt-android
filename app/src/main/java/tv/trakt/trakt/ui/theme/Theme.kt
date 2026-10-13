package tv.trakt.trakt.ui.theme

import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.staticCompositionLocalOf
import timber.log.Timber
import tv.trakt.trakt.common.ui.theme.colors.DarkColors
import tv.trakt.trakt.common.ui.theme.colors.LightColors
import tv.trakt.trakt.common.ui.theme.colors.TraktColors
import tv.trakt.trakt.ui.extensions.isAtLeastLarge
import tv.trakt.trakt.ui.extensions.isAtLeastMedium
import tv.trakt.trakt.ui.theme.model.SeasonalTheme

internal val LocalTraktColors = staticCompositionLocalOf { TraktColors() }
internal val LocalTraktTypography = staticCompositionLocalOf { TraktTypography() }
internal val LocalTraktSpacing = staticCompositionLocalOf { TraktSpacing() }
internal val LocalTraktSize = staticCompositionLocalOf { TraktSize() }
internal val LocalSeasonalTheme = staticCompositionLocalOf<SeasonalTheme?> { null }

internal object TraktTheme {
    val colors: TraktColors
        @Composable
        get() = LocalTraktColors.current

    val typography: TraktTypography
        @Composable
        get() = LocalTraktTypography.current

    val spacing: TraktSpacing
        @Composable
        get() = LocalTraktSpacing.current

    val size: TraktSize
        @Composable
        get() = LocalTraktSize.current
}

@Composable
internal fun TraktTheme(
    colors: TraktColors = DarkColors,
    content: @Composable () -> Unit,
) {
    val windowClass = currentWindowAdaptiveInfo().windowSizeClass

    LaunchedEffect(windowClass.minWidthDp) {
        Timber.i("Window width: ${windowClass.minWidthDp}dp")
    }

    CompositionLocalProvider(
        LocalTraktColors provides colors,
        LocalTraktTypography provides Typography,
        LocalTraktSpacing provides when {
            windowClass.isAtLeastLarge() -> LargeSpacing
            windowClass.isAtLeastMedium() -> MediumSpacing
            else -> Spacing
        },
        LocalTraktSize provides when {
            windowClass.isAtLeastLarge() -> LargeSize
            windowClass.isAtLeastMedium() -> MediumSize
            else -> Size
        },
        content = content,
    )
}

@Composable
internal fun WithoutSeasonalTheme(content: @Composable () -> Unit) {
    val colors = when {
        TraktTheme.colors.isLight -> LightColors
        else -> DarkColors
    }

    CompositionLocalProvider(
        LocalTraktColors provides colors,
        LocalSeasonalTheme provides null,
        content = content,
    )
}
