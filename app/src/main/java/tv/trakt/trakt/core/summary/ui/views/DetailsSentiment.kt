package tv.trakt.trakt.core.summary.ui.views

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.DefaultShadowColor
import androidx.compose.ui.graphics.RadialGradientShader
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.ColorImage
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.model.Sentiments
import tv.trakt.trakt.common.model.Sentiments.Sentiment
import tv.trakt.trakt.common.ui.theme.colors.Purple100
import tv.trakt.trakt.common.ui.theme.colors.Purple900
import tv.trakt.trakt.common.ui.theme.colors.Red100
import tv.trakt.trakt.common.ui.theme.colors.Shade920
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

private val sentimentShape = RoundedCornerShape(24.dp)

@Composable
internal fun DetailsSentiment(
    sentiments: Sentiments,
    modifier: Modifier = Modifier,
) {
    val radialGradient = remember {
        object : ShaderBrush() {
            override fun createShader(size: Size): Shader {
                return RadialGradientShader(
                    colors = listOf(
                        Purple900,
                        Shade920,
                    ),
                    center = Offset(size.width / 4, size.height * 1.5F),
                    radius = size.width * 1.2F,
                )
            }
        }
    }

    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = spacedBy(20.dp, CenterVertically),
        modifier = modifier
            .shadow(
                elevation = 4.dp,
                shape = sentimentShape,
                ambientColor = DefaultShadowColor.copy(alpha = 0.66F),
                spotColor = DefaultShadowColor.copy(alpha = 0.66F),
            )
            .background(
                brush = radialGradient,
                shape = sentimentShape,
            )
            .padding(20.dp),
    ) {
        Row(
            horizontalArrangement = spacedBy(16.dp, Alignment.Start),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_thumb_up_fill),
                contentDescription = null,
                tint = Purple100,
            )
            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = spacedBy(6.dp),
            ) {
                for (sentiment in sentiments.good) {
                    Row(
                        horizontalArrangement = spacedBy(6.dp, Alignment.Start),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Text(
                            text = "•",
                            color = Purple100,
                            style = TraktTheme.typography.paragraphSmaller,
                        )

                        Text(
                            text = sentiment.sentiment.replaceFirstChar { it.titlecase() },
                            color = Purple100,
                            style = TraktTheme.typography.paragraphSmaller,
                        )
                    }
                }
            }
        }

        Row(
            horizontalArrangement = spacedBy(16.dp, Alignment.Start),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_thumb_down_fill),
                contentDescription = null,
                tint = Red100,
            )
            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = spacedBy(6.dp),
            ) {
                for (sentiment in sentiments.bad) {
                    Row(
                        horizontalArrangement = spacedBy(6.dp, Alignment.Start),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Text(
                            text = "•",
                            color = Red100,
                            style = TraktTheme.typography.paragraphSmaller,
                        )

                        Text(
                            text = sentiment.sentiment.replaceFirstChar { it.titlecase() },
                            color = Red100,
                            style = TraktTheme.typography.paragraphSmaller,
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun DetailsSentimentSkeleton(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "infiniteTransition")
    val shimmerTransition by infiniteTransition
        .animateColor(
            initialValue = TraktTheme.colors.skeletonContainer,
            targetValue = TraktTheme.colors.skeletonShimmer,
            animationSpec = infiniteRepeatable(
                animation = tween(1000),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "shimmerTransition",
        )

    Box(
        modifier = modifier
            .heightIn(min = 164.dp)
            .background(
                color = shimmerTransition,
                shape = sentimentShape,
            ),
    )
}

@OptIn(ExperimentalCoilApi::class)
@Preview(
    showBackground = true,
    backgroundColor = 0xFF131517,
    widthDp = 300,
)
@Composable
private fun Preview() {
    TraktTheme {
        val previewHandler = AsyncImagePreviewHandler {
            ColorImage(Color.Blue.toArgb())
        }
        CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
            DetailsSentiment(
                modifier = Modifier.padding(16.dp),
                sentiments = Sentiments(
                    good = listOf(
                        Sentiment("lorem ipsum dolor sit amet consectetur adipiscing elit"),
                        Sentiment("atmospheric"),
                        Sentiment("Stylish"),
                    ).toImmutableList(),
                    bad = listOf(
                        Sentiment("Slow"),
                        Sentiment("Boring"),
                        Sentiment("Confusing"),
                    ).toImmutableList(),
                ),
            )
        }
    }
}

@OptIn(ExperimentalCoilApi::class)
@Preview(
    showBackground = true,
    backgroundColor = 0xFF131517,
    widthDp = 300,
)
@Composable
private fun Preview2() {
    TraktTheme {
        DetailsSentimentSkeleton(
            modifier = Modifier.padding(16.dp),
        )
    }
}
