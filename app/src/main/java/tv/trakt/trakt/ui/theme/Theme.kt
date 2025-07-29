package tv.trakt.trakt.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import tv.trakt.trakt.ui.theme.colors.DarkColors
import tv.trakt.trakt.ui.theme.colors.TraktColors

internal val LocalTraktColors = staticCompositionLocalOf { TraktColors() }
internal val LocalTraktTypography = staticCompositionLocalOf { TraktTypography() }
internal val LocalTraktSpacing = staticCompositionLocalOf { TraktSpacing() }
internal val LocalTraktSize = staticCompositionLocalOf { TraktSize() }

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
fun TraktTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalTraktColors provides DarkColors,
        LocalTraktTypography provides Typography,
        LocalTraktSpacing provides Spacing,
        LocalTraktSize provides Size,
        content = content,
    )
}
