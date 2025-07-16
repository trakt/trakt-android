package tv.trakt.app.tv.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import tv.trakt.app.tv.ui.theme.colors.DarkColors
import tv.trakt.app.tv.ui.theme.colors.TraktColors

val LocalTraktColors = staticCompositionLocalOf { TraktColors() }
val LocalTraktTypography = staticCompositionLocalOf { TraktTypography() }
val LocalTraktSpacing = staticCompositionLocalOf { TraktSpacing() }
val LocalTraktSize = staticCompositionLocalOf { TraktSize() }

object TraktTheme {
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
