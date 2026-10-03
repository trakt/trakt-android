@file:Suppress("ktlint:standard:filename")

package tv.trakt.trakt.helpers.extensions

import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.ui.theme.colors.LightColors
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun TraktThemeLightDark(content: @Composable () -> Unit) {
    Column(
        verticalArrangement = spacedBy(16.dp),
    ) {
        TraktTheme {
            content()
        }

        TraktTheme(
            colors = LightColors,
        ) {
            Box(
                modifier = Modifier.background(
                    TraktTheme.colors.backgroundPrimary,
                ),
            ) {
                content()
            }
        }
    }
}

/**
 * True when the user turned animations off in system settings (reduced motion).
 */
@Composable
internal fun rememberAnimationsDisabled(): Boolean {
    val context = LocalContext.current
    return remember {
        Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f,
        ) == 0f
    }
}
