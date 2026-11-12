package tv.trakt.trakt.core.summary.ui.views.insights

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.draw.rotate
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight.Companion.W600
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.ColorImage
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.extensions.stripMarkdownLinks
import tv.trakt.trakt.common.helpers.extensions.toMarkdownText
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.Sentiments
import tv.trakt.trakt.common.model.Sentiments.Overall.Mixed
import tv.trakt.trakt.common.model.Sentiments.Overall.Negative
import tv.trakt.trakt.common.model.Sentiments.Overall.Positive
import tv.trakt.trakt.common.model.realitycheck.RealityCheck
import tv.trakt.trakt.helpers.extensions.TraktThemeLightDark
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.DefaultCardShape
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun DetailsSentiment(
    sentiments: Sentiments?,
    realityCheck: RealityCheck?,
    vip: Boolean,
    modifier: Modifier = Modifier,
    onRealityCheckClick: () -> Unit = {},
) {
    val containerColor = TraktTheme.colors.sentimentsContainer
    val containerColor2 = TraktTheme.colors.dialogContainer

    val radialGradient = remember {
        object : ShaderBrush() {
            override fun createShader(size: Size): Shader {
                return RadialGradientShader(
                    colors = listOf(
                        containerColor,
                        containerColor2,
                    ),
                    center = Offset(size.width / 4, size.height * 1.5F),
                    radius = size.width * 1.2F,
                )
            }
        }
    }

    val accentColor = TraktTheme.colors.sentimentsAccent
    val sentimentColor = when (sentiments?.overall) {
        Negative -> TraktTheme.colors.sentimentsBadAccent
        Positive, Mixed, null -> accentColor
    }

    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = spacedBy(0.dp, CenterVertically),
        modifier = modifier
            .shadow(
                elevation = TraktTheme.colors.shadowDefault,
                shape = DefaultCardShape,
                ambientColor = DefaultShadowColor.copy(alpha = 0.66F),
                spotColor = DefaultShadowColor.copy(alpha = 0.66F),
            )
            .background(
                brush = radialGradient,
                shape = DefaultCardShape,
            )
            .padding(20.dp),
    ) {
        if (sentiments != null && !sentiments.isEmpty) {
            SentimentSummary(
                sentiments = sentiments,
                sentimentColor = sentimentColor,
            )
        } else if (realityCheck != null) {
            RealityCheckSummary(
                realityCheck = realityCheck,
                color = accentColor,
            )
        }

        if (!vip) {
            realityCheck?.let {
                RealityTierPill(
                    tier = it.verdict,
                    color = accentColor,
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .onClick(onClick = onRealityCheckClick),
                )
            }
            GetVipView()
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top = when {
                            realityCheck != null -> 12.dp
                            else -> 8.dp
                        },
                    ),
            ) {
                realityCheck?.let {
                    RealityTierPill(
                        tier = it.verdict,
                        color = accentColor,
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(top = 8.dp)
                            .onClick(onClick = onRealityCheckClick),
                    )
                }
                Icon(
                    painter = painterResource(R.drawable.ic_back_arrow),
                    contentDescription = null,
                    tint = sentimentColor,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(20.dp)
                        .rotate(180F),
                )
            }
        }
    }
}

@Composable
private fun SentimentSummary(
    sentiments: Sentiments,
    sentimentColor: Color,
) {
    Column {
        Row(
            horizontalArrangement = spacedBy(10.dp, Alignment.Start),
            verticalAlignment = CenterVertically,
        ) {
            Icon(
                painter = painterResource(sentiments.overall.displayIconRes),
                contentDescription = null,
                tint = sentimentColor,
                modifier = Modifier.size(22.dp),
            )
            Text(
                text = stringResource(sentiments.overall.displayTextRes),
                color = sentimentColor,
                style = TraktTheme.typography.heading5.copy(
                    fontSize = 15.sp,
                ),
            )
        }

        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = spacedBy(8.dp),
            modifier = Modifier.padding(top = 16.dp),
        ) {
            val prosCons = remember(sentiments) {
                when (sentiments.overall) {
                    Positive -> sentiments.pros.take(4)
                    Mixed -> sentiments.pros.take(2) + sentiments.cons.take(2)
                    Negative -> sentiments.cons.take(4)
                }
            }
            for (value in prosCons) {
                Row(
                    horizontalArrangement = spacedBy(6.dp, Alignment.Start),
                    verticalAlignment = Alignment.Top,
                ) {
                    Text(
                        text = "•",
                        color = sentimentColor,
                        style = TraktTheme.typography.paragraphSmall,
                    )

                    Text(
                        text = value.value,
                        color = sentimentColor,
                        style = TraktTheme.typography.paragraphSmall,
                    )
                }
            }
        }
    }
}

@Composable
private fun RealityCheckSummary(
    realityCheck: RealityCheck,
    color: Color,
) {
    Column(
        verticalArrangement = spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.header_reality_check),
            color = color,
            style = TraktTheme.typography.heading5.copy(
                fontSize = 15.sp,
            ),
        )
        Text(
            text = remember(realityCheck.summary) {
                realityCheck.summary.stripMarkdownLinks().toMarkdownText()
            },
            color = color,
            style = TraktTheme.typography.paragraphSmall,
            maxLines = 5,
            overflow = TextOverflow.Ellipsis,
        )
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
            .heightIn(min = 180.dp)
            .background(
                color = shimmerTransition,
                shape = DefaultCardShape,
            ),
    )
}

@Composable
private fun GetVipView() {
    Row(
        verticalAlignment = CenterVertically,
        horizontalArrangement = spacedBy(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp)
            .background(
                color = TraktTheme.colors.textPrimary.copy(alpha = 0.1F),
                shape = RoundedCornerShape(18.dp),
            )
            .padding(start = 16.dp, end = 14.dp)
            .padding(vertical = 12.dp),
    ) {
        Column(
            verticalArrangement = spacedBy(3.dp),
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = stringResource(R.string.text_vip_upsell_dive_deeper),
                color = TraktTheme.colors.textPrimary,
                style = TraktTheme.typography.paragraphSmall.copy(
                    fontWeight = W600,
                ),
            )
            Text(
                text = stringResource(R.string.text_vip_upsell_sentiment),
                color = TraktTheme.colors.textPrimary,
                style = TraktTheme.typography.paragraphSmaller.copy(
                    fontSize = 12.sp,
                ),
            )
        }

        Icon(
            painter = painterResource(R.drawable.ic_back_arrow),
            contentDescription = null,
            tint = TraktTheme.colors.textPrimary,
            modifier = Modifier
                .size(20.dp)
                .rotate(180F),
        )
    }
}

@OptIn(ExperimentalCoilApi::class)
@Preview(
    showBackground = true,
    backgroundColor = 0xFF131517,
    widthDp = 350,
)
@Composable
private fun Preview() {
    TraktThemeLightDark {
        val previewHandler = AsyncImagePreviewHandler {
            ColorImage(Color.Blue.toArgb())
        }
        CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
            DetailsSentiment(
                modifier = Modifier.padding(16.dp),
                vip = true,
                realityCheck = PreviewData.realityCheck,
                sentiments = Sentiments(
                    overall = Positive,
                    pros = listOf(
                        Sentiments.Theme(
                            value = "Lorem ipsum dolor sit amet consectetur adipiscing elit",
                            confidence = 0.9F,
                        ),
                        Sentiments.Theme(
                            value = "Stylish",
                            confidence = 0.8F,
                        ),
                    ).toImmutableList(),
                    cons = listOf(
                        Sentiments.Theme(
                            value = "Lorem ipsum dolor sit amet consectetur adipiscing elit",
                            confidence = 0.9F,
                        ),
                        Sentiments.Theme(
                            value = "Stylish",
                            confidence = 0.8F,
                        ),
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
    widthDp = 350,
)
@Composable
private fun Preview2() {
    TraktThemeLightDark {
        val previewHandler = AsyncImagePreviewHandler {
            ColorImage(Color.Blue.toArgb())
        }
        CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
            DetailsSentiment(
                modifier = Modifier.padding(16.dp),
                vip = false,
                realityCheck = PreviewData.realityCheck,
                sentiments = Sentiments(
                    overall = Mixed,
                    pros = listOf(
                        Sentiments.Theme(
                            value = "Lorem ipsum dolor sit amet consectetur adipiscing elit",
                            confidence = 0.9F,
                        ),
                        Sentiments.Theme(
                            value = "Stylish",
                            confidence = 0.8F,
                        ),
                    ).toImmutableList(),
                    cons = listOf(
                        Sentiments.Theme(
                            value = "Lorem ipsum dolor sit amet consectetur adipiscing elit",
                            confidence = 0.9F,
                        ),
                        Sentiments.Theme(
                            value = "Stylish",
                            confidence = 0.8F,
                        ),
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
    widthDp = 350,
)
@Composable
private fun Preview3() {
    TraktTheme {
        val previewHandler = AsyncImagePreviewHandler {
            ColorImage(Color.Blue.toArgb())
        }
        CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
            DetailsSentiment(
                modifier = Modifier.padding(16.dp),
                vip = false,
                realityCheck = null,
                sentiments = Sentiments(
                    overall = Negative,
                    pros = listOf(
                        Sentiments.Theme(
                            value = "Lorem ipsum dolor sit amet consectetur adipiscing elit",
                            confidence = 0.9F,
                        ),
                        Sentiments.Theme(
                            value = "Stylish",
                            confidence = 0.8F,
                        ),
                    ).toImmutableList(),
                    cons = listOf(
                        Sentiments.Theme(
                            value = "Lorem ipsum dolor sit amet consectetur adipiscing elit",
                            confidence = 0.9F,
                        ),
                        Sentiments.Theme(
                            value = "Stylish",
                            confidence = 0.8F,
                        ),
                    ).toImmutableList(),
                ),
            )
        }
    }
}

@PreviewTraktLightDark
@Composable
private fun PreviewRealityCheckOnly() {
    TraktThemePreview {
        DetailsSentiment(
            modifier = Modifier.padding(16.dp),
            vip = true,
            sentiments = null,
            realityCheck = PreviewData.realityCheck,
        )
    }
}

@OptIn(ExperimentalCoilApi::class)
@Preview(
    showBackground = true,
    backgroundColor = 0xFF131517,
    widthDp = 350,
)
@Composable
private fun Preview4() {
    TraktTheme {
        DetailsSentimentSkeleton(
            modifier = Modifier.padding(16.dp),
        )
    }
}
