package tv.trakt.trakt.app.common.ui.buttons

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.tv.material3.Border
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonBorder
import androidx.tv.material3.ButtonColors
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.IconButton
import androidx.tv.material3.Text
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.common.ui.composables.FilmProgressIndicator
import tv.trakt.trakt.resources.R

private val outlineCorner = 12.dp
private val connectedCorner = 6.dp
private val connectedLeadingCorner = 99.dp
private val outlineShape = RoundedCornerShape(outlineCorner)

internal val connectedOutlineSpacing = 5.dp

// Shifts content towards the flat inner edge, where the pill corner leaves more visual room.
internal val connectedOutlineContentOffset = 2.dp
internal val connectedOutlineLeadingShape = RoundedCornerShape(
    topStart = connectedLeadingCorner,
    bottomStart = connectedLeadingCorner,
    topEnd = connectedCorner,
    bottomEnd = connectedCorner,
)
internal val connectedOutlineTrailingShape = RoundedCornerShape(
    topStart = connectedCorner,
    bottomStart = connectedCorner,
    topEnd = connectedLeadingCorner,
    bottomEnd = connectedLeadingCorner,
)
private val outlineWidth = 1.5.dp
private val focusedOutlineWidth = (2.75).dp
private val textButtonHorizontalPadding = 16.dp
private val centeredTextStyle = LineHeightStyle(
    alignment = LineHeightStyle.Alignment.Center,
    trim = LineHeightStyle.Trim.Both,
)

/**
 * Icon-only outlined button matching [PrimaryButton] height and shape. Transparent container,
 * resting outline in the disabled tone, white outline when focused.
 */
@Composable
internal fun OutlineButton(
    icon: Painter,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    iconSize: Dp = 22.dp,
    size: Dp = 42.dp,
    shape: Shape = outlineShape,
    contentOffset: Dp = 0.dp,
    enabled: Boolean = true,
    loading: Boolean = false,
    contentColor: Color = TraktTheme.colors.primaryButtonContent,
    outlineColor: Color = TraktTheme.colors.primaryButtonContainerDisabled,
    disabledContentColor: Color = outlineColor,
    focusedOutlineColor: Color = TraktTheme.colors.primaryButtonContent,
    focusedScale: Float = 1F,
) {
    val activeContentColor = if (enabled) contentColor else disabledContentColor
    val interactive = enabled && !loading

    IconButton(
        modifier = modifier.size(size),
        shape = ButtonDefaults.shape(shape = shape),
        border = outlineBorder(shape, outlineColor, focusedOutlineColor),
        colors = outlineColors(activeContentColor),
        scale = ButtonDefaults.scale(
            focusedScale = focusedScale,
        ),
        onClick = {
            if (interactive) onClick()
        },
    ) {
        if (loading) {
            FilmProgressIndicator(
                size = 18.dp,
                color = activeContentColor,
                modifier = Modifier.offset(x = contentOffset),
            )
        } else {
            Icon(
                painter = icon,
                contentDescription = null,
                tint = activeContentColor,
                modifier = Modifier
                    .offset(x = contentOffset)
                    .size(iconSize),
            )
        }
    }
}

/**
 * Outlined button with a label and optional leading and trailing icons, styled like [OutlineButton].
 */
@Composable
internal fun OutlineTextButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    icon: Painter? = null,
    leadingIcon: Painter? = null,
    iconSize: Dp = 18.dp,
    height: Dp = 42.dp,
    shape: Shape = outlineShape,
    contentOffset: Dp = 0.dp,
    loading: Boolean = false,
    contentColor: Color = TraktTheme.colors.primaryButtonContent,
    outlineColor: Color = TraktTheme.colors.primaryButtonContainerDisabled,
    focusedOutlineColor: Color = TraktTheme.colors.primaryButtonContent,
) {
    Button(
        modifier = modifier.height(height),
        contentPadding = PaddingValues(
            start = textButtonHorizontalPadding + contentOffset,
            end = textButtonHorizontalPadding - contentOffset,
        ),
        shape = ButtonDefaults.shape(shape = shape),
        border = outlineBorder(shape, outlineColor, focusedOutlineColor),
        colors = outlineColors(contentColor),
        scale = ButtonDefaults.scale(
            focusedScale = 1F,
        ),
        onClick = {
            if (!loading) onClick()
        },
    ) {
        Box(contentAlignment = Alignment.Center) {
            Row(
                horizontalArrangement = spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .alpha(if (loading) 0F else 1F)
                    .graphicsLayer {
                        translationY = 1.dp.toPx()
                    },
            ) {
                if (leadingIcon != null) {
                    Icon(
                        painter = leadingIcon,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(iconSize),
                    )
                }

                Text(
                    text = text.uppercase(),
                    color = contentColor,
                    style = TraktTheme.typography.buttonPrimary.copy(
                        lineHeight = 1.em,
                        lineHeightStyle = centeredTextStyle,
                    ),
                    maxLines = 1,
                )

                if (icon != null) {
                    Icon(
                        painter = icon,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(iconSize),
                    )
                }
            }

            if (loading) {
                FilmProgressIndicator(
                    size = iconSize,
                    color = contentColor,
                )
            }
        }
    }
}

@Composable
private fun outlineBorder(
    shape: Shape,
    outlineColor: Color,
    focusedOutlineColor: Color,
): ButtonBorder {
    val restingBorder = Border(
        border = BorderStroke(width = outlineWidth, color = outlineColor),
        shape = shape,
    )
    val focusedBorder = Border(
        border = BorderStroke(width = focusedOutlineWidth, color = focusedOutlineColor),
        shape = shape,
    )

    return ButtonDefaults.border(
        border = restingBorder,
        focusedBorder = focusedBorder,
        pressedBorder = restingBorder,
    )
}

@Composable
private fun outlineColors(contentColor: Color): ButtonColors {
    return ButtonDefaults.colors(
        containerColor = Color.Transparent,
        contentColor = contentColor,
        focusedContainerColor = Color.Transparent,
        focusedContentColor = contentColor,
        pressedContainerColor = Color.Transparent,
        pressedContentColor = contentColor,
    )
}

@Preview
@Composable
private fun Preview1() {
    TraktTheme {
        Row(
            horizontalArrangement = spacedBy(6.dp),
        ) {
            OutlineButton(icon = painterResource(R.drawable.ic_bookmark_off))
            OutlineButton(icon = painterResource(R.drawable.ic_trailer), enabled = false)
            OutlineButton(icon = painterResource(R.drawable.ic_more_vertical), loading = true)
            OutlineTextButton(text = "Label", icon = painterResource(R.drawable.ic_trailer))
        }
    }
}

@Preview
@Composable
private fun PreviewConnected() {
    TraktTheme {
        Row(
            horizontalArrangement = spacedBy(connectedOutlineSpacing),
        ) {
            OutlineTextButton(
                text = "Label",
                shape = connectedOutlineLeadingShape,
                contentOffset = connectedOutlineContentOffset,
            )
            OutlineButton(
                icon = painterResource(R.drawable.ic_more_vertical),
                shape = connectedOutlineTrailingShape,
                contentOffset = -connectedOutlineContentOffset,
            )
        }
    }
}
