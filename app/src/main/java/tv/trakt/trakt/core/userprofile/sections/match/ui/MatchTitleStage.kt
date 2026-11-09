package tv.trakt.trakt.core.userprofile.sections.match.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.compose.ui.zIndex
import coil3.compose.AsyncImage
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.ui.theme.colors.Orange200
import tv.trakt.trakt.common.ui.theme.colors.Orange500
import tv.trakt.trakt.core.userprofile.sections.match.model.MatchStripEntry
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.time.Duration.Companion.milliseconds

private const val WIDEST_FAN = 394F
private const val POSTER_RATIO = 2F / 3F
private const val LARGE_CARD = 120F
private const val SMALL_CARD = 100F

private val EmptyStageHeight = 226.dp
private val ReflectionHeight = 26.dp
private val CardShape = RoundedCornerShape(12.dp)

private data class StageSlot(
    val x: Float,
    val y: Float,
    val tilt: Float,
    val layer: Float,
)

private val Slots: Map<Int, List<StageSlot>> = mapOf(
    1 to listOf(StageSlot(0F, 12F, -2F, 1F)),
    2 to listOf(
        StageSlot(-58F, 14F, -7F, 2F),
        StageSlot(58F, 22F, 6F, 1F),
    ),
    3 to listOf(
        StageSlot(-108F, 30F, -8F, 1F),
        StageSlot(0F, 10F, 0F, 3F),
        StageSlot(108F, 30F, 8F, 2F),
    ),
    4 to listOf(
        StageSlot(-102F, 32F, -8F, 1F),
        StageSlot(-34F, 16F, -3F, 2F),
        StageSlot(34F, 16F, 3F, 3F),
        StageSlot(102F, 32F, 8F, 2F),
    ),
    5 to listOf(
        StageSlot(-134F, 40F, -10F, 1F),
        StageSlot(-68F, 22F, -5F, 2F),
        StageSlot(0F, 14F, 0F, 3F),
        StageSlot(68F, 22F, 5F, 2F),
        StageSlot(134F, 40F, 10F, 1F),
    ),
)

private val Ghosts = listOf(
    StageSlot(-72F, 16F, -7F, 0F),
    StageSlot(0F, 8F, 0F, 0F),
    StageSlot(72F, 16F, 7F, 0F),
)

internal data class SharedMedia(
    val title: String,
    val posterUrl: String?,
    val onClick: () -> Unit,
)

/**
 * @property media Null while the title is still loading.
 */
@Immutable
internal data class MatchStageItem(
    val entry: MatchStripEntry,
    val media: SharedMedia?,
)

@Composable
internal fun MatchTitleStage(
    items: ImmutableList<MatchStageItem>,
    more: Int,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = spacedBy(4.dp),
        horizontalAlignment = CenterHorizontally,
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(
            text = stringResource(R.string.match_drawer_watched_together),
            color = TraktTheme.colors.textSecondary,
            style = TraktTheme.typography.paragraphSmaller.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.fillMaxWidth(),
        )

        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val stageWidth = maxWidth
            val unit = minOf(1.dp, stageWidth / WIDEST_FAN)
            val slots = Slots[items.size].orEmpty()

            if (slots.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(EmptyStageHeight),
                ) {
                    EmptyStage(
                        unit = unit,
                        stageWidth = stageWidth,
                    )
                }
                return@BoxWithConstraints
            }

            val cardWidth = unit * if (items.size > 3) SMALL_CARD else LARGE_CARD
            val fanBottom = slots.maxOf { fanBottom(it, cardWidth) }
            val reflectionTop = fanBottom + 4.dp

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (items.size == 1) reflectionTop + ReflectionHeight else fanBottom),
            ) {
                StageGlow(modifier = Modifier.align(Alignment.TopCenter))
                if (items.size == 1) {
                    StageReflection(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = reflectionTop),
                    )
                }

                val spreads = slots.map { abs(it.x) }.distinct().sorted()
                items.zip(slots).forEach { (item, slot) ->
                    StageCard(
                        item = item,
                        slot = slot,
                        order = spreads.indexOf(abs(slot.x)),
                        width = cardWidth,
                        left = stageWidth / 2 + unit * slot.x - cardWidth / 2,
                    )
                }
            }
        }

        if (more > 0) {
            MorePill(more = more)
        }
    }
}

@Composable
private fun StageCard(
    item: MatchStageItem,
    slot: StageSlot,
    order: Int,
    width: Dp,
    left: Dp,
) {
    val progress by rememberMatchEntrance(
        delay = (300 + order * 60).milliseconds,
        duration = 600.milliseconds,
    )
    val media = item.media

    Box(
        modifier = Modifier
            .zIndex(slot.layer)
            .offset(x = left, y = slot.y.dp)
            .width(width)
            .aspectRatio(POSTER_RATIO)
            .graphicsLayer {
                translationY = (1F - progress) * 20.dp.toPx()
                rotationZ = slot.tilt
                alpha = progress
            }
            .shadow(4.dp, CardShape)
            .clip(CardShape)
            .background(TraktTheme.colors.skeletonContainer),
    ) {
        if (media == null) return@Box

        AsyncImage(
            model = media.posterUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = media.onClick),
        )

        if (item.entry.isFavorite) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .size(24.dp)
                    .background(Color.Black.copy(alpha = 0.55F), CircleShape),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_heart_on),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp),
                )
            }
        }
    }
}

/**
 * Lowest point of a tilted card, so the stage ends where the fan does.
 */
private fun fanBottom(
    slot: StageSlot,
    cardWidth: Dp,
): Dp {
    val cardHeight = cardWidth / POSTER_RATIO
    val tilt = Math.toRadians(slot.tilt.toDouble())
    val tiltedHalfHeight = cardWidth / 2 * abs(sin(tilt)).toFloat() + cardHeight / 2 * abs(cos(tilt)).toFloat()
    return slot.y.dp + cardHeight / 2 + tiltedHalfHeight
}

@Composable
private fun StageGlow(modifier: Modifier = Modifier) {
    val progress by rememberMatchEntrance(delay = 900.milliseconds, duration = 900.milliseconds)

    Canvas(
        modifier = modifier
            .padding(top = 30.dp)
            .size(width = 240.dp, height = 170.dp)
            .graphicsLayer {
                alpha = progress
                val scale = lerp(0.7F, 1F, progress)
                scaleX = scale
                scaleY = scale
            },
    ) {
        scale(scaleX = 1F, scaleY = size.height / size.width) {
            val radius = size.width * 0.5F
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Orange500.copy(alpha = 0.3F), Color.Transparent),
                    center = Offset(size.width * 0.35F, center.y),
                    radius = radius,
                ),
                radius = radius,
                center = Offset(size.width * 0.35F, center.y),
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Orange200.copy(alpha = 0.22F), Color.Transparent),
                    center = Offset(size.width * 0.68F, center.y),
                    radius = radius,
                ),
                radius = radius,
                center = Offset(size.width * 0.68F, center.y),
            )
        }
    }
}

@Composable
private fun StageReflection(modifier: Modifier = Modifier) {
    Canvas(
        modifier = modifier.size(width = 120.dp, height = ReflectionHeight),
    ) {
        scale(scaleX = 1F, scaleY = size.height / size.width) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.Black.copy(alpha = 0.55F), Color.Transparent),
                ),
            )
        }
    }
}

@Composable
private fun EmptyStage(
    unit: Dp,
    stageWidth: Dp,
) {
    val dashColor = TraktTheme.colors.textPrimary.copy(alpha = 0.2F)

    Ghosts.forEach { slot ->
        Box(
            modifier = Modifier
                .offset(x = stageWidth / 2 + unit * (slot.x - SMALL_CARD / 2), y = slot.y.dp)
                .width(unit * SMALL_CARD)
                .aspectRatio(POSTER_RATIO)
                .graphicsLayer { rotationZ = slot.tilt }
                .drawBehind {
                    val radius = 12.dp.toPx()
                    drawRoundRect(
                        color = dashColor,
                        cornerRadius = CornerRadius(radius, radius),
                        style = Stroke(
                            width = 1.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 4.dp.toPx())),
                        ),
                    )
                },
        )
    }

    Text(
        text = stringResource(R.string.match_drawer_nothing_watched_together),
        color = TraktTheme.colors.textSecondary,
        style = TraktTheme.typography.paragraphSmaller,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 178.dp),
    )
}

@Composable
private fun MorePill(more: Int) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .height(30.dp)
            .border(1.dp, TraktTheme.colors.chipContainer, CircleShape)
            .padding(
                start = 12.dp,
                end = 14.dp,
            ),
    ) {
        Text(
            text = stringResource(R.string.match_drawer_more_watched_together, more.toString()),
            color = TraktTheme.colors.textPrimary,
            style = TraktTheme.typography.paragraphSmaller.copy(fontWeight = FontWeight.Bold),
        )
    }
}

@PreviewTraktLightDark
@Composable
private fun Preview() {
    TraktThemePreview {
        MatchTitleStage(
            items = List(5) {
                MatchStageItem(
                    entry = MatchStripEntry(
                        id = TraktId(it),
                        type = MatchStripEntry.Type.Movie,
                        isFavorite = it == 2,
                    ),
                    media = SharedMedia(title = "Title", posterUrl = null, onClick = {}),
                )
            }.toImmutableList(),
            more = 35,
        )
    }
}

@PreviewTraktLightDark
@Composable
private fun EmptyPreview() {
    TraktThemePreview {
        MatchTitleStage(
            items = persistentListOf(),
            more = 0,
        )
    }
}
