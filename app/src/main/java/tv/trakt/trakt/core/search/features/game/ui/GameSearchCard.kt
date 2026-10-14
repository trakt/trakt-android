package tv.trakt.trakt.core.search.features.game.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme
import tv.trakt.trakt.ui.theme.VerticalImageAspectRatio

private val PosterGlow = Color(0xFF7A1F9E)
private val PosterGlowMid = Color(0xFF2A0B33)
private val PosterBase = Color(0xFF0B080D)
private val PosterBlood = Color(0xFFA3132A)
private val PosterInk = Color(0xFFF4F1F6)
private val PosterUp = Color(0xFF4FD38A)
private val PosterDown = Color(0xFFE2384F)
private val PosterAccent = Color(0xFFC26CE8)

private const val POSTER_WIDTH = 300F

private val CardShape = RoundedCornerShape(15.dp)
private val BorderGap = 6.dp
private val CoverShape = RoundedCornerShape(15.dp - BorderGap)

@Composable
internal fun GameSearchCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = spacedBy(10.dp),
        modifier = modifier
            .widthIn(max = TraktTheme.size.verticalMediaCardSize),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(VerticalImageAspectRatio)
                .border(
                    width = 3.dp,
                    color = TraktTheme.colors.accent,
                    shape = CardShape,
                )
                .padding(BorderGap)
                .onClick(onClick = onClick),
        ) {
            GamePosterCover(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CoverShape),
            )
        }

        Row(
            verticalAlignment = CenterVertically,
            horizontalArrangement = spacedBy(3.dp),
            modifier = Modifier.heightIn(min = 16.dp),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_play),
                contentDescription = null,
                tint = TraktTheme.colors.chipContent,
                modifier = Modifier.size(13.dp),
            )
            Text(
                text = stringResource(R.string.header_game_rated_higher),
                style = TraktTheme.typography.cardTitle,
                color = TraktTheme.colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun GamePosterCover(modifier: Modifier = Modifier) {
    val textMeasurer = rememberTextMeasurer()
    val versus = stringResource(R.string.text_game_versus)
    val title = stringResource(R.string.header_game_rated_higher).uppercase()
    val titleStyle = TraktTheme.typography.heading1

    Spacer(
        modifier = modifier.drawWithCache {
            val scale = size.width / POSTER_WIDTH

            fun x(value: Float) = value * scale

            val glowBrush = Brush.radialGradient(
                0F to PosterGlow,
                0.45F to PosterGlowMid,
                1F to PosterBase,
                center = Offset(size.width * 0.5F, size.height * 0.42F),
                radius = size.maxDimension * 0.7F,
            )
            val bloodBrush = Brush.radialGradient(
                colors = listOf(PosterBlood.copy(alpha = 0.55F), PosterBlood.copy(alpha = 0F)),
                center = Offset(size.width * 0.5F, size.height),
                radius = size.maxDimension * 0.6F,
            )

            val upArrow = Path().apply {
                moveTo(x(150F), x(62F))
                lineTo(x(186F), x(104F))
                lineTo(x(114F), x(104F))
                close()
            }
            val downArrow = Path().apply {
                moveTo(x(150F), x(298F))
                lineTo(x(186F), x(256F))
                lineTo(x(114F), x(256F))
                close()
            }

            val heavyStyle = titleStyle.copy(
                fontWeight = FontWeight.W700,
                lineHeight = TextStyle.Default.lineHeight,
            )

            val versusLayout = textMeasurer.measure(
                text = versus,
                style = heavyStyle.copy(
                    color = PosterBase,
                    fontSize = x(42F).toSp(),
                ),
            )

            val titleLayout = textMeasurer.measure(
                text = buildAnnotatedString {
                    if (title.endsWith("?")) {
                        append(title.dropLast(1))
                        withStyle(SpanStyle(color = PosterAccent)) { append("?") }
                    } else {
                        append(title)
                    }
                },
                style = heavyStyle.copy(
                    color = PosterInk,
                    fontSize = x(48F).toSp(),
                    lineHeight = x(48F).toSp(),
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                constraints = Constraints(maxWidth = (size.width - x(48F)).toInt()),
            )

            onDrawBehind {
                drawRect(glowBrush)
                drawRect(bloodBrush)
                drawPath(upArrow, PosterUp)
                drawPath(downArrow, PosterDown)
                drawCircle(
                    color = PosterInk,
                    radius = x(58F),
                    center = Offset(x(150F), x(180F)),
                )
                drawText(
                    textLayoutResult = versusLayout,
                    topLeft = Offset(
                        x = x(150F) - versusLayout.size.width / 2F,
                        y = x(180F) - versusLayout.size.height / 2F,
                    ),
                )
                drawText(
                    textLayoutResult = titleLayout,
                    topLeft = Offset(
                        x = x(24F),
                        y = x(432F) - titleLayout.size.height,
                    ),
                )
            }
        },
    )
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFF131517,
    widthDp = 160,
)
@Composable
private fun Preview() {
    TraktTheme {
        GameSearchCard(
            onClick = {},
        )
    }
}
