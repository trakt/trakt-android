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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.text.HtmlCompat
import androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY
import coil3.ColorImage
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.extensions.toAnnotatedString
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.vip.VipChip
import tv.trakt.trakt.ui.theme.TraktTheme

private val triviaShape = RoundedCornerShape(24.dp)

@Composable
internal fun DetailsTrivia(
    items: ImmutableList<String>,
    vip: Boolean,
    modifier: Modifier = Modifier,
    onVipClick: () -> Unit = {},
) {
    val containerColor = TraktTheme.colors.triviaContainer
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

    Column(
        verticalArrangement = spacedBy(14.dp),
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = triviaShape,
                ambientColor = DefaultShadowColor.copy(alpha = 0.66F),
                spotColor = DefaultShadowColor.copy(alpha = 0.66F),
            )
            .background(
                brush = radialGradient,
                shape = triviaShape,
            )
            .padding(20.dp),
    ) {
        items.forEach { item ->
            Row(
                horizontalArrangement = spacedBy(8.dp, Alignment.Start),
                verticalAlignment = Alignment.Top,
            ) {
                Text(
                    text = "•",
                    color = TraktTheme.colors.triviaAccent,
                    style = TraktTheme.typography.paragraphSmall,
                )

                Text(
                    text = remember(item) {
                        HtmlCompat
                            .fromHtml(item, FROM_HTML_MODE_LEGACY)
                            .toAnnotatedString()
                    },
                    color = TraktTheme.colors.triviaAccent,
                    style = TraktTheme.typography.paragraphSmall,
                )
            }
        }

        if (!vip) {
            GetVipView(
                onVipClick = onVipClick,
            )
        }

        if (vip) {
            Icon(
                painter = painterResource(R.drawable.ic_back_arrow),
                contentDescription = null,
                tint = TraktTheme.colors.textPrimary,
                modifier = Modifier
                    .align(Alignment.End)
                    .size(20.dp)
                    .rotate(180F),
            )
        }
    }
}

@Composable
private fun GetVipView(onVipClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = spacedBy(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp)
            .background(
                color = TraktTheme.colors.textPrimary.copy(alpha = 0.1F),
                shape = RoundedCornerShape(18.dp),
            )
            .onClick(onClick = onVipClick)
            .padding(start = 16.dp, end = 14.dp)
            .padding(vertical = 12.dp),
    ) {
        Column(
            verticalArrangement = spacedBy(3.dp),
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = stringResource(R.string.text_vip_trivia_upsell_1),
                color = TraktTheme.colors.textPrimary,
                style = TraktTheme.typography.paragraphSmall.copy(
                    fontWeight = W600,
                ),
            )
            Text(
                text = stringResource(R.string.text_vip_trivia_upsell_2),
                color = TraktTheme.colors.textPrimary,
                style = TraktTheme.typography.paragraphSmaller.copy(
                    fontSize = 12.sp,
                ),
            )
        }

        VipChip(
            text = stringResource(R.string.badge_text_get_vip),
            onClick = onVipClick,
        )
    }
}

@Composable
internal fun DetailsTriviaSkeleton(modifier: Modifier = Modifier) {
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
            .fillMaxWidth()
            .heightIn(min = 160.dp)
            .background(
                color = shimmerTransition,
                shape = triviaShape,
            ),
    )
}

// -- Previews --

@OptIn(ExperimentalCoilApi::class)
@Preview(
    device = "id:pixel_7",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview() {
    TraktTheme {
        val previewHandler = AsyncImagePreviewHandler {
            ColorImage(Color.Blue.toArgb())
        }
        CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
            DetailsTrivia(
                modifier = Modifier.padding(16.dp),
                vip = false,
                items = listOf(
                    "The famous chase scene was filmed in a single take over three days.",
                    "This scene reveals the true identity of the killer.",
                    "The lead actor improvised most of their dialogue in the final act.",
                ).toImmutableList(),
            )
        }
    }
}

@OptIn(ExperimentalCoilApi::class)
@Preview(
    showBackground = true,
    backgroundColor = 0xFF131517,
    device = "id:pixel_7",
)
@Composable
private fun PreviewSkeleton() {
    TraktTheme {
        DetailsTriviaSkeleton(
            modifier = Modifier.padding(16.dp),
        )
    }
}
