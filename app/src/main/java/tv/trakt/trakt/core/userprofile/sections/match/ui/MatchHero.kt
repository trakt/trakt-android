package tv.trakt.trakt.core.userprofile.sections.match.ui

import android.os.Build
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.core.userprofile.sections.match.model.matchLabelRes
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.time.Duration.Companion.milliseconds
import androidx.compose.ui.graphics.Canvas as BitmapCanvas

private const val MIN_FACE_SWEEP = 16F
private const val MAX_FACE_SWEEP = 180F - MIN_FACE_SWEEP
private const val TINT_SATURATION = 1.6F

private val HeroShape = RoundedCornerShape(24.dp)
private val HeroHeight = 272.dp
private val RingSize = 148.dp
private val RingRadius = 56.dp
private val RingStroke = 6.dp
private val PlateSize = 160.dp
private val FaceSize = 36.dp
private val HalftoneTile = 6.dp
private val HalftoneDot = 2.dp

private val GrayscaleFilter = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0F) })
private val SaturateFilter = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(TINT_SATURATION) })

@Composable
internal fun MatchHero(
    score: Int,
    viewer: User?,
    user: User,
    backdropUrl: String?,
    modifier: Modifier = Modifier,
) {
    val base = TraktTheme.colors.backgroundPrimary

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(HeroHeight)
            .shadow(TraktTheme.colors.shadowLarge, HeroShape, clip = false)
            .clip(HeroShape)
            .background(base),
    ) {
        HeroCover(
            backdropUrl = backdropUrl,
            base = base,
        )

        Column(
            horizontalAlignment = CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 16.dp),
        ) {
            MatchRing(
                score = score,
                viewer = viewer,
                user = user,
                base = base,
            )
            HeroLabel(
                label = stringResource(matchLabelRes(score)),
                modifier = Modifier.padding(top = 12.dp),
            )
            Text(
                text = stringResource(R.string.match_drawer_overlap_caption),
                color = TraktTheme.colors.textSecondary,
                style = TraktTheme.typography.paragraphSmaller,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun HeroCover(
    backdropUrl: String?,
    base: Color,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (backdropUrl != null) {
            HalftoneImage(url = backdropUrl)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                AsyncImage(
                    model = backdropUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    colorFilter = SaturateFilter,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { blendMode = BlendMode.Color }
                        .blur(32.dp, BlurredEdgeTreatment.Rectangle),
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(base.copy(alpha = 0.45F)),
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(120.dp)
                .background(
                    Brush.verticalGradient(
                        0F to Color.Transparent,
                        0.62F to base.copy(alpha = 0.92F),
                    ),
                ),
        )
    }
}

@Composable
private fun HalftoneImage(url: String) {
    val density = LocalDensity.current
    val inkIn by rememberMatchEntrance(delay = 0.milliseconds, duration = 900.milliseconds)

    val dots = remember(density) {
        with(density) {
            val tile = HalftoneTile.roundToPx()
            val bitmap = ImageBitmap(tile, tile)
            BitmapCanvas(bitmap).drawCircle(
                center = Offset(tile / 2F, tile / 2F),
                radius = HalftoneDot.toPx(),
                paint = Paint().apply { isAntiAlias = true },
            )
            ShaderBrush(ImageShader(bitmap, TileMode.Repeated, TileMode.Repeated))
        }
    }

    AsyncImage(
        model = url,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        colorFilter = GrayscaleFilter,
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                alpha = inkIn
                compositingStrategy = CompositingStrategy.Offscreen
            }
            .drawWithContent {
                drawContent()
                drawRect(brush = dots, blendMode = BlendMode.DstIn)
            },
    )
}

@Composable
private fun MatchRing(
    score: Int,
    viewer: User?,
    user: User,
    base: Color,
) {
    val progress by rememberMatchEntrance(delay = 250.milliseconds, duration = 950.milliseconds)
    val sweep = faceSweep(score) * progress
    val trackColor = TraktTheme.colors.textPrimary.copy(alpha = 0.18F)
    val inkColor = TraktTheme.colors.textPrimary

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(RingSize)
            .drawBehind {
                val plateRadius = PlateSize.toPx() / 2
                drawCircle(
                    color = base.copy(alpha = 0.38F),
                    radius = plateRadius + 10.dp.toPx(),
                )
                drawCircle(
                    color = base.copy(alpha = 0.84F),
                    radius = plateRadius,
                )
            },
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = RingRadius.toPx()
            val stroke = RingStroke.toPx()
            val topLeft = Offset(center.x - radius, center.y - radius)
            val arcSize = Size(radius * 2, radius * 2)

            drawCircle(
                color = trackColor,
                radius = radius,
                style = Stroke(width = stroke),
            )
            if (score > 0) {
                listOf(sweep, -sweep).forEach {
                    drawArc(
                        color = inkColor,
                        startAngle = 90F,
                        sweepAngle = it,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = stroke, cap = StrokeCap.Round),
                    )
                }
            }
        }

        val angle = Math.toRadians(sweep.toDouble())
        val reach = RingRadius * sin(angle).toFloat()
        val drop = RingRadius * cos(angle).toFloat()

        MatchFace(
            user = viewer,
            base = base,
            modifier = Modifier.offset(x = -reach, y = drop),
        )
        MatchFace(
            user = user,
            base = base,
            modifier = Modifier.offset(x = reach, y = drop),
        )

        Row {
            Text(
                text = (score * progress).roundToInt().toString(),
                color = TraktTheme.colors.textPrimary,
                style = TraktTheme.typography.heading2.copy(
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                ),
                maxLines = 1,
                modifier = Modifier.alignByBaseline(),
            )
            Text(
                text = "%",
                color = TraktTheme.colors.textSecondary,
                style = TraktTheme.typography.paragraphSmaller,
                modifier = Modifier.alignByBaseline(),
            )
        }
    }
}

@Composable
private fun MatchFace(
    user: User?,
    base: Color,
    modifier: Modifier = Modifier,
) {
    val faceModifier = modifier
        .size(FaceSize)
        .border(3.dp, base, CircleShape)
        .padding(1.dp)
        .clip(CircleShape)

    if (user?.hasAvatar == true) {
        AsyncImage(
            model = user.images?.avatar?.full,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            error = painterResource(R.drawable.ic_person_placeholder),
            modifier = faceModifier,
        )
    } else {
        Image(
            painter = painterResource(R.drawable.ic_person_placeholder),
            contentDescription = null,
            modifier = faceModifier,
        )
    }
}

@Composable
private fun HeroLabel(
    label: String,
    modifier: Modifier = Modifier,
) {
    val progress by rememberMatchEntrance(delay = 200.milliseconds, duration = 700.milliseconds)

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
    ) {
        Text(
            text = label,
            color = TraktTheme.colors.textPrimary,
            style = TraktTheme.typography.heading1.copy(
                fontSize = 40.sp,
                lineHeight = 52.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.025F).em,
            ),
            textAlign = TextAlign.Center,
            maxLines = 1,
            softWrap = false,
            autoSize = TextAutoSize.StepBased(
                minFontSize = 24.sp,
                maxFontSize = 40.sp,
            ),
            modifier = Modifier.graphicsLayer {
                alpha = progress
                translationY = (1F - progress) * 4.dp.toPx()
                val scale = 0.92F + 0.08F * progress
                scaleX = scale
                scaleY = scale
            },
        )
    }
}

private fun faceSweep(score: Int): Float {
    return (score * 1.8F).coerceIn(MIN_FACE_SWEEP, MAX_FACE_SWEEP)
}

@PreviewTraktLightDark
@Composable
private fun Preview() {
    TraktThemePreview {
        MatchHero(
            score = 87,
            viewer = PreviewData.user1,
            user = PreviewData.user1,
            backdropUrl = null,
        )
    }
}
