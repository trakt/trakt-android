@file:Suppress("ktlint:standard:property-naming")

package tv.trakt.trakt.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.launch
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.helpers.extensions.TraktThemeLightDark
import tv.trakt.trakt.helpers.extensions.rememberAnimationsDisabled
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.PillShape
import tv.trakt.trakt.ui.theme.TraktTheme

private val TrackInset = 4.dp
private val TrackBorder = 1.dp
private val ThumbSpec = spring<Float>(
    dampingRatio = 0.6F,
    stiffness = Spring.StiffnessMediumLow,
)

private data class ThumbBounds(
    val start: Float,
    val width: Float,
)

@Composable
internal fun <T> SegmentedToggle(
    options: ImmutableList<T>,
    selected: T?,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    fillWidth: Boolean = false,
    height: Dp = TraktTheme.size.trackHeight,
    content: @Composable RowScope.(option: T, selected: Boolean) -> Unit,
) {
    val bounds = remember(options) { mutableStateMapOf<Int, ThumbBounds>() }
    val target = bounds[options.indexOf(selected)]
    val thumbStart = remember { Animatable(0F) }
    val thumbWidth = remember { Animatable(0F) }
    val animationsDisabled = rememberAnimationsDisabled()

    LaunchedEffect(target) {
        if (target == null) return@LaunchedEffect
        if (thumbWidth.value == 0F || animationsDisabled) {
            thumbStart.snapTo(target.start)
            thumbWidth.snapTo(target.width)
            return@LaunchedEffect
        }
        launch { thumbStart.animateTo(target.start, ThumbSpec) }
        thumbWidth.animateTo(target.width, ThumbSpec)
    }

    val selectorContainer = TraktTheme.colors.selectorContainer
    val selectorBorder = TraktTheme.colors.selectorBorder

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(0.dp),
        modifier = modifier
            .alpha(if (enabled) 1F else 0.4F)
            .height(height)
            .background(TraktTheme.colors.trackContainer, PillShape)
            .border(TrackBorder, TraktTheme.colors.trackBorder, PillShape)
            .padding(TrackInset)
            .drawBehind {
                if (thumbWidth.value == 0F) return@drawBehind
                val stroke = TrackBorder.toPx()
                val radius = CornerRadius(size.height / 2)
                drawRoundRect(
                    color = selectorContainer,
                    topLeft = Offset(thumbStart.value, 0F),
                    size = Size(thumbWidth.value, size.height),
                    cornerRadius = radius,
                )
                drawRoundRect(
                    color = selectorBorder,
                    topLeft = Offset(thumbStart.value + stroke / 2, stroke / 2),
                    size = Size(thumbWidth.value - stroke, size.height - stroke),
                    cornerRadius = radius,
                    style = Stroke(width = stroke),
                )
            },
    ) {
        options.forEachIndexed { index, option ->
            val isSelected = option == selected
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .then(if (fillWidth) Modifier.weight(1F) else Modifier)
                    .fillMaxHeight()
                    .onPlaced {
                        bounds[index] = ThumbBounds(
                            start = it.positionInParent().x,
                            width = it.size.width.toFloat(),
                        )
                    }
                    .clip(PillShape)
                    .onClick(enabled = enabled && !isSelected) { onSelect(option) }
                    .padding(horizontal = 12.dp),
            ) {
                CompositionLocalProvider(
                    LocalContentColor provides when {
                        isSelected -> TraktTheme.colors.selectorContent
                        else -> TraktTheme.colors.textPrimary
                    },
                ) {
                    content(option, isSelected)
                }
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    TraktThemeLightDark {
        SegmentedToggle(
            options = persistentListOf("Episodes", "Info", "Reviews"),
            selected = "Info",
            onSelect = {},
            modifier = Modifier.padding(16.dp),
        ) { option, _ ->
            Icon(
                painter = painterResource(R.drawable.ic_check_2),
                contentDescription = null,
            )
            Text(
                text = option,
                style = TraktTheme.typography.buttonTertiary,
                modifier = Modifier.padding(start = 5.dp),
            )
        }
    }
}
