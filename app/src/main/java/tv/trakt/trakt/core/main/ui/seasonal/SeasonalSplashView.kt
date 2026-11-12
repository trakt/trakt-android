package tv.trakt.trakt.core.main.ui.seasonal

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import tv.trakt.trakt.common.ui.theme.colors.LightColors
import tv.trakt.trakt.common.ui.theme.colors.Orange100
import tv.trakt.trakt.common.ui.theme.colors.Orange300
import tv.trakt.trakt.common.ui.theme.colors.Orange400
import tv.trakt.trakt.common.ui.theme.colors.Orange700
import tv.trakt.trakt.core.main.ui.seasonal.christmas.ChristmasSplash
import tv.trakt.trakt.core.main.ui.seasonal.halloween.HalloweenEmbers
import tv.trakt.trakt.core.main.ui.seasonal.halloween.HalloweenHero
import tv.trakt.trakt.core.main.ui.seasonal.newyear.NewYearSplash
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme
import tv.trakt.trakt.ui.theme.model.SeasonalTheme

private val ScrimColor = Color(0xFF0C0603).copy(alpha = 0.95F)

@Composable
internal fun SeasonalSplashView(
    enabled: Boolean,
    modifier: Modifier = Modifier,
    viewModel: SeasonalSplashViewModel = koinViewModel(),
    onDismissed: () -> Unit = {},
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var dismissed by remember { mutableStateOf<SeasonalTheme?>(null) }

    val season = state.season?.takeIf { it != dismissed }

    SeasonalSplashContent(
        season = season?.takeIf { enabled },
        modifier = modifier,
        onDismiss = {
            season?.let {
                dismissed = it
                viewModel.dismiss(it)
                onDismissed()
            }
        },
    )
}

@Composable
private fun SeasonalSplashContent(
    season: SeasonalTheme?,
    modifier: Modifier = Modifier,
    animate: Boolean = rememberSplashAnimate(),
    onDismiss: () -> Unit = {},
) {
    BackHandler(enabled = season != null, onBack = onDismiss)

    AnimatedVisibility(
        visible = season != null,
        enter = fadeIn(tween(150)),
        exit = fadeOut(tween(150)),
        modifier = modifier,
    ) {
        when (season) {
            SeasonalTheme.Halloween -> HalloweenSplash(
                animate = animate,
                onDismiss = onDismiss,
            )
            SeasonalTheme.Christmas -> ChristmasSplash(
                animate = animate,
                onDismiss = onDismiss,
            )
            SeasonalTheme.NewYear -> NewYearSplash(
                animate = animate,
                onDismiss = onDismiss,
            )
            SeasonalTheme.Doomsday, null -> Unit
        }
    }
}

@Composable
private fun HalloweenSplash(
    animate: Boolean,
    onDismiss: () -> Unit,
) {
    SplashFrame(
        scrimColor = ScrimColor,
        glowColor = Orange700,
        animate = animate,
        onDismiss = onDismiss,
    ) {
        if (animate) {
            HalloweenEmbers(modifier = Modifier.fillMaxSize())
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp),
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .padding(top = 48.dp, bottom = 96.dp),
        ) {
            HalloweenHero(animate = animate)
            SplashGreeting(
                animate = animate,
                text = stringResource(R.string.text_seasonal_greeting_halloween),
                signoffColor = Orange300,
            ) { intro ->
                HalloweenTitle(intro = intro)
            }
        }
    }
}

@Composable
private fun HalloweenTitle(intro: () -> Float) {
    Text(
        text = stringResource(R.string.header_seasonal_greeting_halloween),
        style = TraktTheme.typography.heading1.copy(
            fontSize = 40.sp,
            lineHeight = 42.sp,
            fontWeight = FontWeight.W900,
            brush = Brush.linearGradient(listOf(Orange400, Orange100, Orange400)),
        ),
        textAlign = TextAlign.Center,
        modifier = Modifier.graphicsLayer {
            val progress = intro()
            val scale = lerp(0.8F, 1F, progress)
            alpha = progress
            scaleX = scale
            scaleY = scale
        },
    )
}

@Preview(device = "id:pixel_5")
@Composable
private fun Preview() {
    TraktTheme {
        SeasonalSplashContent(
            season = SeasonalTheme.Halloween,
            animate = false,
        )
    }
}

@Preview(device = "id:pixel_5")
@Composable
private fun PreviewLight() {
    TraktTheme(colors = LightColors) {
        SeasonalSplashContent(
            season = SeasonalTheme.Halloween,
            animate = false,
        )
    }
}

@Preview(device = "id:pixel_5")
@Composable
private fun ChristmasPreview() {
    TraktTheme {
        SeasonalSplashContent(
            season = SeasonalTheme.Christmas,
            animate = false,
        )
    }
}

@Preview(device = "id:pixel_5")
@Composable
private fun ChristmasPreviewLight() {
    TraktTheme(colors = LightColors) {
        SeasonalSplashContent(
            season = SeasonalTheme.Christmas,
            animate = false,
        )
    }
}

@Preview(device = "id:pixel_5")
@Composable
private fun NewYearPreview() {
    TraktTheme {
        SeasonalSplashContent(
            season = SeasonalTheme.NewYear,
            animate = false,
        )
    }
}
