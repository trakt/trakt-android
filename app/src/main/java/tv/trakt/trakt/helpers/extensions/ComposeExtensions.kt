@file:Suppress("ktlint:standard:filename")

package tv.trakt.trakt.helpers.extensions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
