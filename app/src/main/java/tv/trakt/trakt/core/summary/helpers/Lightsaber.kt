package tv.trakt.trakt.core.summary.helpers

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.hapticfeedback.HapticFeedbackType.Companion.ToggleOff
import androidx.compose.ui.hapticfeedback.HapticFeedbackType.Companion.ToggleOn
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.zIndex
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import kotlinx.coroutines.delay
import tv.trakt.trakt.app.BuildConfig
import tv.trakt.trakt.common.firebase.FirebaseConfig.RemoteKey.STAR_WARS_SABER
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme
import kotlin.time.Duration.Companion.milliseconds

private val StarWarsMatches = setOf(
    "star wars",
    "andor",
    "ahsoka",
    "the mandalorian",
    "the mandalorian & grogu",
    "the mandalorian and grogu",
    "the book of boba fett",
    "obi-wan kenobi",
    "the acolyte",
    "skeleton crew",
    "maul: shadow lord",
    "starfighter",
    "droids",
    "ewoks",
    "caravan of courage: an ewok adventure",
    "ewoks: the battle for endor",
)

@Suppress("KotlinConstantConditions", "SimplifyBooleanWithConstants")
internal fun hasLightsaber(title: String): Boolean {
    val enabled = Firebase.remoteConfig.getBoolean(STAR_WARS_SABER) || BuildConfig.FLAVOR == "internal"
    return enabled && StarWarsMatches.any { title.contains(it, ignoreCase = true) }
}

@Composable
internal fun Lightsaber(modifier: Modifier = Modifier) {
    val accent = TraktTheme.colors.accentPalette.shade600
    val beamShape = RoundedCornerShape(
        topStart = 0.dp,
        bottomStart = 0.dp,
        topEnd = 100.dp,
        bottomEnd = 100.dp,
    )
    var ignited by rememberSaveable { mutableStateOf(true) }
    var initialIgnition by rememberSaveable { mutableStateOf(true) }
    val ignition = remember { Animatable(if (ignited && !initialIgnition) 1F else 0F) }
    val haptic = LocalHapticFeedback.current

    val hum by rememberInfiniteTransition(label = "lightsaberHum").animateFloat(
        initialValue = 0F,
        targetValue = 0F,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 3000
                0F at 0
                1F at 900
                0.65F at 1300
                0.85F at 1800
                0F at 3000
            },
            repeatMode = RepeatMode.Reverse,
        ),
        label = "lightsaberHumGlow",
    )

    LaunchedEffect(ignited) {
        if (initialIgnition) {
            initialIgnition = false
            delay(500.milliseconds)
        }
        ignition.animateTo(
            targetValue = if (ignited) 1F else 0F,
            animationSpec = tween(
                durationMillis = if (ignited) 300 else 150,
                easing = FastOutSlowInEasing,
            ),
        )
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .onClick(throttle = false) {
                ignited = !ignited
                haptic.performHapticFeedback(if (ignited) ToggleOn else ToggleOff)
            },
    ) {
        Image(
            painter = painterResource(
                if (ignited) R.drawable.ic_lightsaber_on else R.drawable.ic_lightsaber_off,
            ),
            contentDescription = null,
            modifier = Modifier
                .height(14.dp)
                .aspectRatio(429.93F / 94.35F)
                .zIndex(99F),
        )

        Box(
            modifier = Modifier.weight(1F),
        ) {
            val glow = hum * ignition.value
            Box(
                modifier = Modifier
                    .fillMaxWidth(ignition.value)
                    .height(6.dp)
                    .dropShadow(
                        shape = beamShape,
                        shadow = Shadow(
                            radius = lerp(2.dp, 5.dp, glow),
                            color = accent,
                            spread = lerp(1.2.dp, 3.dp, glow),
                            alpha = 0.8F + (0.2F * glow),
                        ),
                    )
                    .padding(end = 1.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            listOf(accent, TraktTheme.colors.accentPalette.shade100, accent),
                        ),
                        shape = beamShape,
                    ),
            )
        }
    }
}

@PreviewTraktLightDark
@Composable
private fun Preview() {
    TraktThemePreview {
        Lightsaber(
            modifier = Modifier.padding(16.dp),
        )
    }
}
