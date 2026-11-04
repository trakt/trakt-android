package tv.trakt.trakt.core.main.ui.seasonal.christmas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.ui.theme.colors.Red700
import tv.trakt.trakt.core.main.ui.seasonal.SplashFrame
import tv.trakt.trakt.core.main.ui.seasonal.SplashGreeting
import tv.trakt.trakt.resources.R

private val ScrimColor = Color(0xFF050406).copy(alpha = 0.95F)

@Composable
internal fun ChristmasSplash(
    animate: Boolean,
    onDismiss: () -> Unit,
) {
    SplashFrame(
        scrimColor = ScrimColor,
        glowColor = Red700,
        animate = animate,
        onDismiss = onDismiss,
        // The close button sits over the snowy hills, so it needs its own dark backdrop.
        closeButtonModifier = Modifier.background(ScrimColor.copy(alpha = 0.6F), CircleShape),
    ) {
        if (animate) {
            ChristmasSnow(modifier = Modifier.fillMaxSize())
        }

        ChristmasHills(
            animate = animate,
            modifier = Modifier.align(Alignment.BottomCenter),
        )

        if (animate) {
            SantaSleigh()
        }

        ChristmasGarland(
            animate = animate,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .windowInsetsPadding(WindowInsets.statusBars),
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp),
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .padding(top = 96.dp, bottom = 160.dp),
        ) {
            ChristmasHero(animate = animate)
            SplashGreeting(
                animate = animate,
                text = stringResource(R.string.text_seasonal_greeting_christmas),
                signoffColor = Gold300,
            ) { intro ->
                ChristmasBanner(
                    text = stringResource(R.string.header_seasonal_greeting_christmas),
                    intro = intro,
                )
            }
        }
    }
}
