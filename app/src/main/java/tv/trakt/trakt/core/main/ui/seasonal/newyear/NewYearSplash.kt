package tv.trakt.trakt.core.main.ui.seasonal.newyear

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.ui.theme.colors.Amber700
import tv.trakt.trakt.core.main.ui.seasonal.Gold300
import tv.trakt.trakt.core.main.ui.seasonal.SplashFrame
import tv.trakt.trakt.core.main.ui.seasonal.SplashGarland
import tv.trakt.trakt.core.main.ui.seasonal.SplashGreeting
import tv.trakt.trakt.core.main.ui.seasonal.SplashGround
import tv.trakt.trakt.core.main.ui.seasonal.SplashGroundSize
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.model.SeasonalTheme
import java.time.LocalDate

private val ScrimColor = Color(0xFF050406).copy(alpha = 0.95F)
private val SkylineSize = SplashGroundSize(aspectRatio = 1280F / 260F, maxHeight = 260.dp)

@Composable
internal fun NewYearSplash(
    animate: Boolean,
    onDismiss: () -> Unit,
) {
    val year = remember { SeasonalTheme.NewYear.seasonYear(LocalDate.now()) + 1 }

    SplashFrame(
        scrimColor = ScrimColor,
        glowColor = Amber700,
        animate = animate,
        onDismiss = onDismiss,
    ) {
        if (animate) {
            NewYearParticles(modifier = Modifier.fillMaxSize())
        }

        SplashGround(
            image = R.drawable.img_new_year_skyline,
            size = SkylineSize,
            animate = animate,
            modifier = Modifier.align(Alignment.BottomCenter),
        )

        SplashGarland(
            image = R.drawable.img_new_year_bunting,
            height = 72.dp,
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
                .padding(top = 72.dp, bottom = 120.dp),
        ) {
            NewYearHero(animate = animate)
            SplashGreeting(
                animate = animate,
                text = stringResource(R.string.text_seasonal_greeting_new_year),
                signoffColor = Gold300,
            ) { intro ->
                NewYearTitle(
                    year = year,
                    animate = animate,
                    intro = intro,
                )
            }
        }
    }
}
