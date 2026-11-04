package tv.trakt.trakt.core.main.ui.seasonal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun SplashGreeting(
    animate: Boolean,
    text: String,
    signoffColor: Color,
    title: @Composable (intro: () -> Float) -> Unit,
) {
    val titleIntro = rememberSplashIntro(
        animate = animate,
        delayMillis = 900,
        durationMillis = 800,
        easing = SplashEaseOutQuint,
    )
    val textIntro = rememberSplashIntro(animate = animate, delayMillis = 1_200, durationMillis = 600)
    val signoffIntro = rememberSplashIntro(animate = animate, delayMillis = 1_400, durationMillis = 600)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.widthIn(max = 320.dp),
    ) {
        title { titleIntro.value }
        Text(
            text = text,
            style = TraktTheme.typography.paragraphSmall,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(top = 8.dp)
                .riseIn { textIntro.value },
        )
        Text(
            text = stringResource(R.string.text_seasonal_greeting_signoff).uppercase(),
            style = TraktTheme.typography.meta.copy(letterSpacing = 2.sp),
            color = signoffColor,
            textAlign = TextAlign.Center,
            modifier = Modifier.riseIn { signoffIntro.value },
        )
    }
}

private fun Modifier.riseIn(progress: () -> Float): Modifier {
    return graphicsLayer {
        val value = progress()
        alpha = value
        translationY = (1F - value) * 12.dp.toPx()
    }
}
