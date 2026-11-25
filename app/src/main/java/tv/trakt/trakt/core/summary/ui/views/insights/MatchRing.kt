package tv.trakt.trakt.core.summary.ui.views.insights

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.common.model.MediaMatch
import tv.trakt.trakt.common.ui.theme.colors.Purple400
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

private const val RING_VIEWPORT = 40F
private const val RING_RADIUS = 16F
private const val RING_STROKE = 4F

private val BadgeSize = 18.dp
private val BadgeOffset = 4.dp

@Immutable
internal data class InsightsMatch(
    val score: Int?,
    val avatarUrl: String?,
)

internal enum class MatchRingSize(
    val ring: Dp,
    val fontSize: TextUnit,
) {
    Card(44.dp, 12.sp),
    Drawer(56.dp, 15.sp),
}

@Composable
internal fun MatchRing(
    score: Int?,
    modifier: Modifier = Modifier,
    size: MatchRingSize = MatchRingSize.Card,
    badge: (@Composable () -> Unit)? = null,
) {
    val trackColor = TraktTheme.colors.textSecondary.copy(alpha = 0.25F)
    val valueColor = when {
        score != null && score < MediaMatch.LOW_THRESHOLD -> TraktTheme.colors.textSecondary
        else -> Purple400
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(size.ring),
    ) {
        Canvas(modifier = Modifier.size(size.ring)) {
            val scale = this.size.minDimension / RING_VIEWPORT
            val radius = RING_RADIUS * scale
            val stroke = Stroke(width = RING_STROKE * scale, cap = StrokeCap.Round)

            drawCircle(
                color = trackColor,
                radius = radius,
                style = Stroke(width = RING_STROKE * scale),
            )

            if (score != null) {
                drawArc(
                    color = valueColor,
                    startAngle = -90F,
                    sweepAngle = 360F * score / 100F,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = stroke,
                )
            }
        }

        Text(
            text = buildAnnotatedString {
                if (score == null) {
                    append("-")
                } else {
                    append(score.toString())
                    withStyle(SpanStyle(fontSize = 0.75.em)) {
                        append("%")
                    }
                }
            },
            color = TraktTheme.colors.textPrimary,
            style = TraktTheme.typography.heading6.copy(
                fontSize = size.fontSize,
                lineHeight = size.fontSize,
            ),
        )

        if (badge != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = BadgeOffset, y = BadgeOffset)
                    .size(BadgeSize),
            ) {
                badge()
            }
        }
    }
}

@Composable
internal fun MatchAvatar(
    avatarUrl: String?,
    modifier: Modifier = Modifier,
) {
    AsyncImage(
        model = avatarUrl,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        placeholder = painterResource(R.drawable.ic_person_placeholder),
        error = painterResource(R.drawable.ic_person_placeholder),
        fallback = painterResource(R.drawable.ic_person_placeholder),
        modifier = modifier
            .size(BadgeSize)
            .border(2.dp, TraktTheme.colors.dialogContainer, CircleShape)
            .clip(CircleShape),
    )
}

@PreviewTraktLightDark
@Composable
private fun Preview() {
    TraktThemePreview {
        Row(
            horizontalArrangement = spacedBy(16.dp),
            modifier = Modifier.padding(16.dp),
        ) {
            MatchRing(score = 87, badge = { MatchAvatar(avatarUrl = null) })
            MatchRing(score = 25)
            MatchRing(score = null, badge = { MatchAvatar(avatarUrl = null) })
            MatchRing(score = 64, size = MatchRingSize.Drawer)
        }
    }
}
