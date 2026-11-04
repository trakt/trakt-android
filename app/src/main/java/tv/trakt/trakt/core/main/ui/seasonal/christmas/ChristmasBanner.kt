package tv.trakt.trakt.core.main.ui.seasonal.christmas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import tv.trakt.trakt.common.ui.theme.colors.Red400
import tv.trakt.trakt.common.ui.theme.colors.Red600
import tv.trakt.trakt.common.ui.theme.colors.Red700
import tv.trakt.trakt.common.ui.theme.colors.Red800
import tv.trakt.trakt.common.ui.theme.colors.Red900
import tv.trakt.trakt.common.ui.theme.colors.Shade10
import tv.trakt.trakt.core.main.ui.seasonal.Gold100
import tv.trakt.trakt.core.main.ui.seasonal.Gold200
import tv.trakt.trakt.core.main.ui.seasonal.Gold300
import tv.trakt.trakt.core.main.ui.seasonal.Gold50
import tv.trakt.trakt.core.main.ui.seasonal.Gold600
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

private val TailWidth = 32.dp
private val TailDrop = 16.dp
private val EdgeWidth = 2.dp
private val BannerShape = RoundedCornerShape(4.dp)

private val BannerBrush = Brush.verticalGradient(
    0F to Red400,
    0.55F to Red600,
    1F to Red800,
)
private val TailBrush = Brush.verticalGradient(listOf(Red700, Red900))
private val TitleBrush = Brush.linearGradient(listOf(Gold100, Shade10, Gold50, Shade10, Gold200))

@Composable
internal fun ChristmasBanner(
    text: String,
    intro: () -> Float,
    modifier: Modifier = Modifier,
) {
    val shadowOffset = with(LocalDensity.current) { EdgeWidth.toPx() }

    Box(
        modifier = modifier
            .unfurl(intro)
            .padding(horizontal = TailWidth)
            .padding(bottom = TailDrop)
            .drawBehind { drawTails() }
            .background(BannerBrush, BannerShape)
            .drawBehind { drawEdges(color = Gold300, top = 0F, height = size.height) }
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Text(
            text = text,
            style = TraktTheme.typography.heading1.copy(
                fontSize = 32.sp,
                lineHeight = 36.sp,
                fontWeight = FontWeight.W900,
                fontStyle = FontStyle.Italic,
                brush = TitleBrush,
            ),
            textAlign = TextAlign.Center,
        )
    }
}

private fun Modifier.unfurl(intro: () -> Float): Modifier {
    return graphicsLayer { alpha = intro().coerceIn(0F, 1F) }
        .drawWithContent {
            val progress = intro()
            val inset = lerp(size.width / 2, -size.width, progress)
            clipRect(
                left = inset,
                top = -size.height,
                right = size.width - inset,
                bottom = size.height * 2,
            ) {
                this@drawWithContent.drawContent()
            }
        }
}

private fun DrawScope.drawTails() {
    val tailWidth = TailWidth.toPx()
    val top = TailDrop.toPx()
    val height = size.height

    listOf(
        startTailPath(left = -tailWidth, top = top, width = tailWidth, height = height),
        endTailPath(left = size.width, top = top, width = tailWidth, height = height),
    ).forEach { path ->
        clipPath(path) {
            drawRect(
                brush = TailBrush,
                topLeft = Offset(-tailWidth, top),
                size = Size(size.width + tailWidth * 2, height),
            )
            drawEdges(color = Gold600, top = top, height = height, extra = tailWidth)
        }
    }
}

private fun DrawScope.drawEdges(
    color: Color,
    top: Float,
    height: Float,
    extra: Float = 0F,
) {
    val edge = EdgeWidth.toPx()
    val edgeSize = Size(size.width + extra * 2, edge)
    drawRect(color = color, topLeft = Offset(-extra, top), size = edgeSize)
    drawRect(color = color, topLeft = Offset(-extra, top + height - edge), size = edgeSize)
}

private fun startTailPath(
    left: Float,
    top: Float,
    width: Float,
    height: Float,
): Path {
    return Path().apply {
        moveTo(left, top)
        lineTo(left + width, top)
        lineTo(left + width, top + height)
        lineTo(left, top + height)
        lineTo(left + width * 0.36F, top + height / 2)
        close()
    }
}

private fun endTailPath(
    left: Float,
    top: Float,
    width: Float,
    height: Float,
): Path {
    return Path().apply {
        moveTo(left, top)
        lineTo(left + width, top)
        lineTo(left + width * 0.64F, top + height / 2)
        lineTo(left + width, top + height)
        lineTo(left, top + height)
        close()
    }
}

@Preview
@Composable
private fun Preview() {
    TraktTheme {
        Box(
            modifier = Modifier
                .background(Color.Black)
                .padding(24.dp),
        ) {
            ChristmasBanner(
                text = stringResource(R.string.header_seasonal_greeting_christmas),
                intro = { 1F },
            )
        }
    }
}
