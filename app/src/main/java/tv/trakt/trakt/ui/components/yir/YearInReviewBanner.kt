package tv.trakt.trakt.ui.components.yir

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.ui.theme.colors.Shade930
import tv.trakt.trakt.helpers.extensions.rememberAnimationsDisabled
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.buttons.PrimaryButton
import tv.trakt.trakt.ui.theme.TraktTheme
import kotlin.math.ceil
import tv.trakt.trakt.R as AppR

private val BackgroundTop = Color(0xFF1C1730)
private val BackgroundBottom = Shade930
private val PosterShape = RoundedCornerShape(12.dp)
private val DotColor = Color.White.copy(alpha = 0.07F)
private val DotSpacing = 14.dp
private val DotSize = 1.5.dp

private const val POSTER_RATIO = 1200F / 1680F
private const val RIBBON_ROTATION = -6F
private const val RIBBON_CYCLE_MILLIS = 200_000

private data class Poster(
    @param:DrawableRes val drawable: Int,
    val rotation: Float,
)

private val TopRibbon = persistentListOf(
    Poster(AppR.drawable.yir_anime_voyager, rotation = -4F),
    Poster(AppR.drawable.yir_critic, rotation = 2F),
    Poster(AppR.drawable.yir_weekend_marathoner, rotation = -1F),
    Poster(AppR.drawable.yir_omnivore, rotation = 3F),
)

private val BottomRibbon = persistentListOf(
    Poster(AppR.drawable.yir_opening_act, rotation = 2F),
    Poster(AppR.drawable.yir_cinephile, rotation = -3F),
    Poster(AppR.drawable.yir_loyalist, rotation = 1F),
    Poster(AppR.drawable.yir_comfort_rewatcher, rotation = -2F),
)

@Composable
internal fun YearInReviewBanner(
    year: Int,
    modifier: Modifier = Modifier,
    onCheckClick: () -> Unit = {},
    onCloseClick: () -> Unit = {},
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BackgroundTop, BackgroundBottom)))
            .dotGrid(),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = 24.dp,
                    start = 12.dp,
                    end = 12.dp,
                ),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_trakt_logo),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.height(32.dp),
            )

            Icon(
                painter = painterResource(R.drawable.ic_close),
                contentDescription = stringResource(R.string.button_label_close),
                tint = Color.White,
                modifier = Modifier
                    .padding(end = 8.dp)
                    .align(Alignment.CenterEnd)
                    .size(20.dp)
                    .onClick(onClick = onCloseClick),
            )
        }

        PosterRibbons(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1F),
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = spacedBy(48.dp),
            modifier = Modifier.padding(
                start = 24.dp,
                end = 24.dp,
                top = 16.dp,
                bottom = 32.dp,
            ),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = spacedBy(8.dp),
            ) {
                Text(
                    text = stringResource(R.string.header_year_in_review_banner, year),
                    style = TraktTheme.typography.heading2,
                    color = TraktTheme.colors.textPrimary,
                    textAlign = TextAlign.Center,
                )

                Text(
                    text = stringResource(R.string.text_year_in_review_banner_tagline),
                    style = TraktTheme.typography.paragraphSmall,
                    color = TraktTheme.colors.textSecondary,
                    textAlign = TextAlign.Center,
                )
            }

            PrimaryButton(
                text = stringResource(R.string.button_text_continue),
                onClick = onCheckClick,
                contentColor = Color.White,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

private fun Modifier.dotGrid(): Modifier =
    drawWithCache {
        val spacing = DotSpacing.toPx()
        val columns = (size.width / spacing).toInt()
        val rows = (size.height / spacing).toInt()
        val inset = Offset(
            x = (size.width - columns * spacing) / 2F,
            y = (size.height - rows * spacing) / 2F,
        )
        val points = (0..rows).flatMap { row ->
            (0..columns).map { column ->
                inset + Offset(column * spacing, row * spacing)
            }
        }

        onDrawBehind {
            drawPoints(
                points = points,
                pointMode = PointMode.Points,
                color = DotColor,
                strokeWidth = DotSize.toPx(),
                cap = StrokeCap.Round,
            )
        }
    }

// Masks the content to transparent so the background gradient and dots show through.
private fun Modifier.fadeOutBottom(): Modifier =
    graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
        .drawWithContent {
            drawContent()
            drawRect(
                brush = Brush.verticalGradient(
                    0.8F to Color.Black,
                    1F to Color.Transparent,
                ),
                blendMode = BlendMode.DstIn,
            )
        }

@Composable
private fun PosterRibbons(modifier: Modifier = Modifier) {
    BoxWithConstraints(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clipToBounds()
            .fadeOutBottom(),
    ) {
        val posterWidth = min(maxWidth * 0.32F, 180.dp)
        val posterSpacing = posterWidth * 0.08F
        val cycleWidth = (posterWidth + posterSpacing) * TopRibbon.size

        // Each ribbon must overhang both screen edges by one full cycle so the loop never shows a gap.
        val copies = ceil(maxWidth * 1.3F / cycleWidth).toInt() + 2

        val progress = rememberRibbonProgress()

        Column(
            verticalArrangement = spacedBy(posterSpacing * 1.5F),
            modifier = Modifier.graphicsLayer {
                rotationZ = RIBBON_ROTATION
            },
        ) {
            PosterRibbon(
                posters = TopRibbon,
                copies = copies,
                posterWidth = posterWidth,
                spacing = posterSpacing,
                modifier = Modifier
                    .offset(x = posterWidth * 0.1F)
                    .graphicsLayer {
                        translationX = (progress() - 0.5F) * cycleWidth.toPx()
                    },
            )
            PosterRibbon(
                posters = BottomRibbon,
                copies = copies,
                posterWidth = posterWidth,
                spacing = posterSpacing,
                modifier = Modifier
                    .offset(x = -posterWidth * 0.25F)
                    .graphicsLayer {
                        translationX = (0.5F - progress()) * cycleWidth.toPx()
                    },
            )
        }
    }
}

@Composable
private fun rememberRibbonProgress(): () -> Float {
    if (rememberAnimationsDisabled()) {
        return { 0.5F }
    }

    val progress = rememberInfiniteTransition(label = "ribbons").animateFloat(
        initialValue = 0F,
        targetValue = 1F,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = RIBBON_CYCLE_MILLIS,
                easing = LinearEasing,
            ),
        ),
        label = "ribbonsProgress",
    )
    return { progress.value }
}

@Composable
private fun PosterRibbon(
    posters: ImmutableList<Poster>,
    copies: Int,
    posterWidth: Dp,
    spacing: Dp,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = spacedBy(spacing),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.wrapContentWidth(unbounded = true),
    ) {
        repeat(copies) {
            posters.forEach { poster ->
                Image(
                    painter = painterResource(poster.drawable),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .width(posterWidth)
                        .aspectRatio(POSTER_RATIO)
                        .graphicsLayer { rotationZ = poster.rotation }
                        .shadow(8.dp, PosterShape)
                        .clip(PosterShape),
                )
            }
        }
    }
}

@Preview(device = "id:pixel_5")
@Composable
private fun Preview() {
    TraktTheme {
        YearInReviewBanner(year = 2026)
    }
}
