package tv.trakt.trakt.core.comments.ui

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults.cardColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.ColorImage
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import tv.trakt.trakt.ui.theme.HorizontalImageAspectRatio
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun CommentCardSkeleton(
    modifier: Modifier = Modifier,
    containerColor: Color = TraktTheme.colors.skeletonContainer,
    shimmerColor: Color = TraktTheme.colors.skeletonShimmer,
    corner: Dp = 24.dp,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "infiniteTransition")
    val shimmerTransition by infiniteTransition
        .animateColor(
            initialValue = containerColor,
            targetValue = shimmerColor,
            animationSpec = infiniteRepeatable(
                animation = tween(1000),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "shimmerTransition",
        )

    Card(
        onClick = {},
        modifier = modifier,
        shape = RoundedCornerShape(corner),
        colors = cardColors(
            containerColor = shimmerTransition,
        ),
        content = {
            CommentCardContent(shimmerColor)
        },
    )
}

@Composable
private fun CommentCardContent(
    shimmerColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = spacedBy(0.dp),
        modifier = modifier
            .padding(vertical = 16.dp)
            .fillMaxSize(),
    ) {
        CommentHeader(
            shimmerColor = shimmerColor,
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        Spacer(modifier = Modifier.weight(1f))

        Box(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(100))
                .background(shimmerColor)
                .fillMaxWidth()
                .height(16.dp),
        )
    }
}

@Composable
private fun CommentHeader(
    shimmerColor: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = spacedBy(12.dp),
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(shimmerColor),
        )

        Column(verticalArrangement = spacedBy(2.dp)) {
            Row(
                horizontalArrangement = spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "",
                    style = TraktTheme.typography.meta,
                    color = Color.Transparent,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth(fraction = 0.5F)
                        .background(
                            color = shimmerColor,
                            shape = RoundedCornerShape(100),
                        ),
                )
            }
            Text(
                text = "",
                style = TraktTheme.typography.meta,
                color = Color.Transparent,
                modifier = Modifier
                    .fillMaxWidth(fraction = 0.75F)
                    .background(
                        color = shimmerColor,
                        shape = RoundedCornerShape(100),
                    ),
            )
        }
    }
}

@OptIn(ExperimentalCoilApi::class)
@Preview
@Composable
private fun Preview() {
    TraktTheme {
        val previewHandler = AsyncImagePreviewHandler {
            ColorImage(Color.LightGray.toArgb())
        }
        CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
            Column(
                verticalArrangement = spacedBy(16.dp),
            ) {
                CommentCardSkeleton(
                    modifier = Modifier
                        .height(TraktTheme.size.commentCardSize)
                        .aspectRatio(HorizontalImageAspectRatio),
                )
            }
        }
    }
}
