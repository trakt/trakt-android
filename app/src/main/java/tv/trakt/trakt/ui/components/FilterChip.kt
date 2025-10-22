package tv.trakt.trakt.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun FilterChip(
    selected: Boolean,
    text: String,
    modifier: Modifier = Modifier,
    leadingIcon: @Composable (() -> Unit)? = null,
    paddingHorizontal: PaddingValues = PaddingValues(
        start = 9.dp,
        end = 13.dp,
    ),
    onClick: () -> Unit = {},
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = spacedBy(0.dp),
        modifier = modifier
            .onClick(onClick = onClick)
            .height(28.dp)
            .border(
                width = 1.dp,
                color = if (selected) {
                    TraktTheme.colors.accent
                } else {
                    TraktTheme.colors.chipContainer
                },
                shape = CircleShape,
            )
            .background(
                shape = RoundedCornerShape(100),
                color = if (selected) {
                    TraktTheme.colors.accent
                } else {
                    Color.Transparent
                },
            )
            .padding(paddingHorizontal),
    ) {
        AnimatedVisibility(
            visible = selected && leadingIcon != null,
            enter = fadeIn(tween(150)) + expandHorizontally(tween(150)),
            exit = fadeOut(tween(150)) + shrinkHorizontally(tween(150)),
        ) {
            leadingIcon?.invoke()
        }
        Text(
            text = text,
            style = TraktTheme.typography.buttonTertiary,
            color = TraktTheme.colors.textPrimary,
            maxLines = 1,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(start = 4.dp),
        )
    }
}

@Composable
internal fun FilterChipSkeleton(modifier: Modifier = Modifier) {
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
            .height(28.dp)
            .width(72.dp)
            .background(
                shape = RoundedCornerShape(100),
                color = shimmerTransition,
            )
            .padding(
                start = 9.dp,
                end = 13.dp,
            ),
    )
}

@Preview
@Composable
private fun Preview() {
    TraktTheme {
        FilterChip(
            selected = false,
            text = "Filter Chip",
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.ic_check_round),
                    contentDescription = null,
                    tint = TraktTheme.colors.textPrimary,
                    modifier = Modifier.size(FilterChipDefaults.IconSize),
                )
            },
        )
    }
}

@Preview
@Composable
private fun Preview2() {
    TraktTheme {
        FilterChip(
            selected = true,
            text = "Selected Chip",
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.ic_check_round),
                    contentDescription = null,
                    tint = TraktTheme.colors.textPrimary,
                    modifier = Modifier.size(FilterChipDefaults.IconSize),
                )
            },
        )
    }
}

@Preview
@Composable
private fun SkeletonPreview() {
    TraktTheme {
        FilterChipSkeleton()
    }
}
