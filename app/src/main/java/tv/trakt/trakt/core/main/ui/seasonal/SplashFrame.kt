package tv.trakt.trakt.core.main.ui.seasonal

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
internal fun SplashFrame(
    scrimColor: Color,
    glowColor: Color,
    animate: Boolean,
    onDismiss: () -> Unit,
    closeButtonModifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(scrimColor)
            .background(
                Brush.radialGradient(
                    0F to glowColor.copy(alpha = 0.45F),
                    0.6F to Color.Transparent,
                ),
            )
            .clickable(
                onClick = {},
                indication = null,
                interactionSource = null,
            ),
    ) {
        content()

        SplashCloseButton(
            autoDismiss = animate,
            onClick = onDismiss,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(bottom = 48.dp)
                .then(closeButtonModifier),
        )
    }
}
