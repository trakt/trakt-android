package tv.trakt.trakt.core.ratings.allratings.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Bottom
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.collections.immutable.persistentMapOf
import tv.trakt.trakt.common.helpers.extensions.rememberThousandsFormat
import tv.trakt.trakt.common.model.ExternalRating
import tv.trakt.trakt.common.ui.theme.colors.Purple200
import tv.trakt.trakt.common.ui.theme.colors.Purple300
import tv.trakt.trakt.common.ui.theme.colors.Purple500
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

private const val MIN_BAR_FRACTION = 0.08F
private val ChartHeight = 72.dp

@Composable
internal fun TraktRatingCard(
    rating: ExternalRating.TraktRating,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = spacedBy(20.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .background(
                color = TraktTheme.colors.dialogOnContainer,
                shape = RoundedCornerShape(16.dp),
            )
            .padding(horizontal = 16.dp, vertical = 13.dp),
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 92.dp)
                .padding(bottom = 4.dp),
        ) {
            Text(
                text = "${rating.ratingPercent}%",
                style = TraktTheme.typography.heading3.copy(
                    fontSize = 32.sp,
                    letterSpacing = 0.02.sp,
                ),
                color = TraktTheme.colors.textPrimary,
                maxLines = 1,
            )
            Text(
                text = stringResource(
                    R.string.text_ratings_votes,
                    rememberThousandsFormat(rating.votes),
                ),
                style = TraktTheme.typography.meta,
                color = TraktTheme.colors.textSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }

        val bars = rating.starDistribution
        val maxValue = bars.values.maxOrNull() ?: 0F

        Column(
            verticalArrangement = spacedBy(6.dp),
            modifier = Modifier.weight(1F),
        ) {
            Row(
                verticalAlignment = Bottom,
                horizontalArrangement = spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ChartHeight),
            ) {
                bars.forEach { (_, value) ->
                    val fraction = when {
                        maxValue > 0 -> (value / maxValue).coerceAtLeast(MIN_BAR_FRACTION)
                        else -> MIN_BAR_FRACTION
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = spacedBy(0.dp, Bottom),
                        modifier = Modifier
                            .weight(1F)
                            .fillMaxHeight(),
                    ) {
                        Text(
                            text = rememberThousandsFormat(value.toInt()),
                            style = TraktTheme.typography.cardTitle.copy(fontSize = 10.sp),
                            color = TraktTheme.colors.textPrimary,
                            maxLines = 1,
                            textAlign = TextAlign.Center,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(bottom = 4.dp),
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(fraction)
                                .background(
                                    color = barColor(fraction),
                                    shape = RoundedCornerShape(6.dp),
                                ),
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                bars.keys.forEach { star ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = spacedBy(0.5.dp, Alignment.CenterHorizontally),
                        modifier = Modifier.weight(1F),
                    ) {
                        Text(
                            text = star.toString(),
                            style = TraktTheme.typography.cardTitle.copy(fontSize = 10.sp),
                            color = TraktTheme.colors.textPrimary,
                            maxLines = 1,
                            textAlign = TextAlign.Center,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Icon(
                            painter = painterResource(R.drawable.ic_star),
                            contentDescription = null,
                            tint = TraktTheme.colors.textPrimary,
                            modifier = Modifier
                                .size(12.dp)
                                .graphicsLayer {
                                    translationY = -0.5.dp.toPx()
                                },
                        )
                    }
                }
            }
        }
    }
}

private fun barColor(fraction: Float): Color {
    return when {
        fraction >= 0.66F -> Purple200
        fraction >= 0.33F -> Purple300
        else -> Purple500
    }
}

@Preview(widthDp = 360)
@Composable
private fun Preview() {
    TraktTheme {
        TraktRatingCard(
            rating = ExternalRating.TraktRating(
                rating = 8.5F,
                votes = 5100,
                distribution = persistentMapOf(
                    1 to 40F,
                    2 to 60F,
                    3 to 80F,
                    4 to 120F,
                    5 to 200F,
                    6 to 300F,
                    7 to 500F,
                    8 to 900F,
                    9 to 1400F,
                    10 to 1500F,
                ),
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        )
    }
}
