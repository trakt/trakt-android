package tv.trakt.trakt.core.ratings.allratings.ui

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.model.Ids
import tv.trakt.trakt.common.model.Rating
import tv.trakt.trakt.common.model.Season
import tv.trakt.trakt.common.model.toSlugId
import tv.trakt.trakt.common.model.toTraktId
import tv.trakt.trakt.common.ui.theme.colors.Purple200
import tv.trakt.trakt.common.ui.theme.colors.Purple500
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

private val GraphHeight = 120.dp
private const val LINE_SPAN_FRACTION = 0.4F

// Beyond this many seasons the graph scrolls horizontally, keeping a minimum
// spacing per data point instead of squeezing all points into the card width.
private const val MAX_FIT_SEASONS = 8
private val MinPointSpacing = 44.dp

@Composable
internal fun QualityOverTimeCard(
    seasons: ImmutableList<Season>,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = spacedBy(8.dp),
        modifier = modifier
            .background(
                color = TraktTheme.colors.dialogOnContainer,
                shape = RoundedCornerShape(16.dp),
            )
            .padding(horizontal = 16.dp)
            .padding(top = 13.dp, bottom = 12.dp),
    ) {
        val peak = seasons.maxBy { it.rating.rating }
        val low = seasons.minBy { it.rating.rating }

        Text(
            text = stringResource(
                R.string.text_ratings_season_extremes_android,
                peak.number,
                "${peak.rating.ratingPercent}%",
                low.number,
                "${low.rating.ratingPercent}%",
            ),
            style = TraktTheme.typography.cardTitle.copy(fontSize = 10.sp),
            color = TraktTheme.colors.textSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .align(Alignment.End)
                .padding(bottom = 8.dp),
        )

        val scrollable = seasons.size > MAX_FIT_SEASONS
        val contentModifier = when {
            scrollable -> Modifier.width(MinPointSpacing * seasons.size)
            else -> Modifier.fillMaxWidth()
        }

        CompositionLocalProvider(LocalOverscrollFactory provides null) {
            Column(
                verticalArrangement = spacedBy(8.dp),
                modifier = when {
                    scrollable -> {
                        Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 3.dp)
                    }
                    else -> {
                        Modifier.fillMaxWidth()
                    }
                },
            ) {
                QualityLineGraph(
                    seasons = seasons,
                    modifier = contentModifier
                        .height(GraphHeight),
                )

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = contentModifier,
                ) {
                    seasons.forEach { season ->
                        Text(
                            text = "S${season.number}",
                            style = TraktTheme.typography.cardTitle.copy(fontSize = 10.sp),
                            color = TraktTheme.colors.textPrimary,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun QualityOverTimeSkeletonCard(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "infiniteTransition")
    val shimmerTransition by infiniteTransition
        .animateColor(
            initialValue = TraktTheme.colors.dialogOnContainer,
            targetValue = TraktTheme.colors.skeletonShimmer,
            animationSpec = infiniteRepeatable(
                animation = tween(1000),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "shimmerTransition",
        )

    Column(
        verticalArrangement = spacedBy(8.dp),
        modifier = modifier
            .background(
                color = shimmerTransition,
                shape = RoundedCornerShape(16.dp),
            )
            .padding(horizontal = 16.dp)
            .padding(top = 13.dp, bottom = 12.dp),
    ) {
        Text(
            text = "",
            style = TraktTheme.typography.cardTitle.copy(fontSize = 10.sp),
            color = TraktTheme.colors.textSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .align(Alignment.End)
                .padding(bottom = 8.dp),
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(GraphHeight),
        )

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            repeat(5) {
                Box(
                    modifier = Modifier
                        .width(18.dp)
                        .height(13.dp),
                )
            }
        }
    }
}

@Composable
private fun QualityLineGraph(
    seasons: ImmutableList<Season>,
    modifier: Modifier = Modifier,
) {
    val dotLabelColor = TraktTheme.colors.textPrimary
    val dotLabelStyle = TraktTheme.typography.cardTitle.copy(fontSize = 10.sp)
    val textMeasurer = rememberTextMeasurer()

    Canvas(modifier = modifier) {
        val ratings = seasons.map { it.rating.ratingPercent }
        val minRating = ratings.min()
        val maxRating = ratings.max()
        val range = (maxRating - minRating).coerceAtLeast(1)

        val topPad = 20.dp.toPx()
        val lineSpan = size.height * LINE_SPAN_FRACTION

        fun yFor(rating: Int): Float {
            val normalized = (rating - minRating).toFloat() / range
            return topPad + (1F - normalized) * lineSpan
        }

        val stepX = size.width / (ratings.size - 1)
        val points = ratings.mapIndexed { index, rating ->
            Offset(stepX * index, yFor(rating))
        }

        val linePath = Path().apply {
            moveTo(points.first().x, points.first().y)
            for (index in 1 until points.size) {
                val previous = points[index - 1]
                val current = points[index]
                val midX = (previous.x + current.x) / 2
                cubicTo(midX, previous.y, midX, current.y, current.x, current.y)
            }
        }

        val fillPath = Path().apply {
            addPath(linePath)
            lineTo(size.width, size.height)
            lineTo(0F, size.height)
            close()
        }

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Purple200.copy(alpha = 0.95F),
                    Color.Transparent,
                ),
            ),
        )

        drawPath(
            path = linePath,
            color = Purple500,
            style = Stroke(
                width = 2.dp.toPx(),
                cap = StrokeCap.Round,
            ),
        )

        points.forEach { point ->
            drawCircle(
                color = Color.White,
                radius = 2.dp.toPx(),
                center = point,
            )
            drawCircle(
                color = Color.White,
                radius = 2.dp.toPx(),
                center = point,
                style = Stroke(width = 1.5.dp.toPx()),
            )
        }

        // Percent value above each dot.
        points.forEachIndexed { index, point ->
            val label = textMeasurer.measure(
                text = AnnotatedString("${ratings[index]}%"),
                style = dotLabelStyle,
            )

            drawText(
                textLayoutResult = label,
                color = dotLabelColor,
                topLeft = Offset(
                    x = (point.x - label.size.width / 2F)
                        .coerceIn(0F, size.width - label.size.width),
                    y = (point.y - 6.dp.toPx() - label.size.height)
                        .coerceAtLeast(0F),
                ),
            )
        }
    }
}

@Preview(widthDp = 360)
@Composable
private fun Preview() {
    TraktTheme {
        QualityOverTimeCard(
            seasons = listOf(85, 88, 84, 90, 96)
                .mapIndexed { index, percent ->
                    Season(
                        ids = Ids(
                            trakt = index.toTraktId(),
                            slug = "".toSlugId(),
                        ),
                        number = index + 1,
                        rating = Rating(
                            rating = percent / 10F,
                            votes = 1000,
                        ),
                        episodeCount = 10,
                        images = null,
                        overview = null,
                        firstAired = null,
                        updatedAt = null,
                    )
                }
                .toImmutableList(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        )
    }
}

@Preview(widthDp = 360)
@Composable
private fun PreviewManySeasons() {
    TraktTheme {
        QualityOverTimeCard(
            seasons = (1..24)
                .map { number ->
                    Season(
                        ids = Ids(
                            trakt = number.toTraktId(),
                            slug = "".toSlugId(),
                        ),
                        number = number,
                        rating = Rating(
                            rating = (7F + (number % 4) * 0.6F),
                            votes = 1000,
                        ),
                        episodeCount = 10,
                        images = null,
                        overview = null,
                        firstAired = null,
                        updatedAt = null,
                    )
                }
                .toImmutableList(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        )
    }
}

@Preview(widthDp = 360)
@Composable
private fun PreviewSkeleton() {
    TraktTheme {
        QualityOverTimeSkeletonCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        )
    }
}
