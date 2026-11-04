package tv.trakt.trakt.core.main.ui.seasonal.newyear

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import tv.trakt.trakt.common.ui.theme.colors.Orange400
import tv.trakt.trakt.common.ui.theme.colors.Orange700
import tv.trakt.trakt.common.ui.theme.colors.Orange900
import tv.trakt.trakt.common.ui.theme.colors.Yellow500
import tv.trakt.trakt.core.main.ui.seasonal.Gold100
import tv.trakt.trakt.core.main.ui.seasonal.Gold200
import tv.trakt.trakt.core.main.ui.seasonal.Gold300
import tv.trakt.trakt.core.main.ui.seasonal.Gold400
import tv.trakt.trakt.core.main.ui.seasonal.Gold50
import tv.trakt.trakt.core.main.ui.seasonal.SplashEaseOutBack
import tv.trakt.trakt.core.main.ui.seasonal.rememberSplashIntro
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

private val YearBrush = Brush.verticalGradient(
    0F to Gold50,
    0.46F to Gold300,
    0.5F to Orange700,
    0.72F to Gold200,
    1F to Yellow500,
)
private val TitleBrush = Brush.linearGradient(listOf(Gold300, Gold50, Orange400, Gold100, Yellow500))

private val RuleWidth = 240.dp
private val RuleHeight = 12.dp
private val RuleStroke = 2.dp
private val DiamondSize = 10.dp

@Composable
internal fun NewYearTitle(
    year: Int,
    animate: Boolean,
    intro: () -> Float,
) {
    val yearIntro = rememberSplashIntro(
        animate = animate,
        delayMillis = 700,
        durationMillis = 900,
        easing = SplashEaseOutBack,
    )
    val ruleIntro = rememberSplashIntro(animate = animate, delayMillis = 1_000, durationMillis = 600)
    val shadowOffset = with(LocalDensity.current) { 2.dp.toPx() }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = year.toString(),
            style = TraktTheme.typography.heading1.copy(
                fontSize = 88.sp,
                lineHeight = 88.sp,
                fontWeight = FontWeight.W900,
                letterSpacing = 0.02.em,
                brush = YearBrush,
                shadow = Shadow(color = Orange900, offset = Offset(0F, shadowOffset)),
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.graphicsLayer {
                val progress = yearIntro.value
                val scale = lerp(2.4F, 1F, progress)
                alpha = progress.coerceIn(0F, 1F)
                scaleX = scale
                scaleY = scale
            },
        )

        Canvas(
            modifier = Modifier
                .width(RuleWidth)
                .height(RuleHeight)
                .graphicsLayer {
                    alpha = ruleIntro.value
                    translationY = (1F - ruleIntro.value) * 12.dp.toPx()
                },
        ) {
            val stroke = RuleStroke.toPx()
            val centerY = size.height / 2
            val lineBrush = Brush.horizontalGradient(
                0F to Color.Transparent,
                0.15F to Gold400,
                0.85F to Gold400,
                1F to Color.Transparent,
            )
            drawLine(
                brush = lineBrush,
                start = Offset(0F, centerY),
                end = Offset(size.width * 0.44F, centerY),
                strokeWidth = stroke,
            )
            drawLine(
                brush = lineBrush,
                start = Offset(size.width * 0.56F, centerY),
                end = Offset(size.width, centerY),
                strokeWidth = stroke,
            )
            val diamond = DiamondSize.toPx()
            rotate(degrees = 45F) {
                drawRect(
                    color = Gold400,
                    topLeft = Offset(center.x - diamond / 2, center.y - diamond / 2),
                    size = Size(diamond, diamond),
                    style = Stroke(width = stroke),
                )
            }
        }

        Text(
            text = stringResource(R.string.header_seasonal_greeting_new_year).uppercase(),
            style = TraktTheme.typography.heading1.copy(
                fontSize = 30.sp,
                lineHeight = 34.sp,
                fontWeight = FontWeight.W700,
                letterSpacing = 0.08.em,
                brush = TitleBrush,
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(top = 4.dp)
                .graphicsLayer {
                    val progress = intro()
                    val scale = lerp(0.8F, 1F, progress)
                    alpha = progress
                    scaleX = scale
                    scaleY = scale
                },
        )
    }
}

@Preview
@Composable
private fun Preview() {
    TraktTheme {
        Column(
            modifier = Modifier
                .background(Color.Black)
                .padding(24.dp),
        ) {
            NewYearTitle(
                year = 2027,
                animate = false,
                intro = { 1F },
            )
        }
    }
}
