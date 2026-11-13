package tv.trakt.trakt.app.common.ui.buttons

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
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

private val outlineShape = RoundedCornerShape(12.dp)
private val outlineWidth = 1.5.dp
private val focusedOutlineWidth = (2.75).dp

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
        shape = ButtonDefaults.shape(shape = outlineShape),
        border = outlineBorder(outlineColor, focusedOutlineColor),
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
            )
        } else {
            Icon(
                painter = icon,
                contentDescription = null,
                tint = activeContentColor,
                modifier = Modifier.size(iconSize),
            )
        }
    }
}

/**
 * Outlined button with a label and an optional trailing icon, styled like [OutlineButton].
 */
@Composable
internal fun OutlineTextButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    icon: Painter? = null,
    iconSize: Dp = 18.dp,
    height: Dp = 42.dp,
    loading: Boolean = false,
    contentColor: Color = TraktTheme.colors.primaryButtonContent,
    outlineColor: Color = TraktTheme.colors.primaryButtonContainerDisabled,
    focusedOutlineColor: Color = TraktTheme.colors.primaryButtonContent,
) {
    Button(
        modifier = modifier.height(height),
        contentPadding = PaddingValues(horizontal = 12.dp),
        shape = ButtonDefaults.shape(shape = outlineShape),
        border = outlineBorder(outlineColor, focusedOutlineColor),
        colors = outlineColors(contentColor),
        scale = ButtonDefaults.scale(
            focusedScale = 1F,
        ),
        onClick = {
            if (!loading) onClick()
        },
    ) {
        Row(
            horizontalArrangement = spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = text.uppercase(),
                color = contentColor,
                style = TraktTheme.typography.buttonPrimary,
                maxLines = 1,
            )

            when {
                loading -> {
                    FilmProgressIndicator(
                        size = iconSize,
                        color = contentColor,
                    )
                }

                icon != null -> {
                    Icon(
                        painter = icon,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(iconSize),
                    )
                }
            }
        }
    }
}

@Composable
private fun outlineBorder(
    outlineColor: Color,
    focusedOutlineColor: Color,
): ButtonBorder {
    val restingBorder = Border(
        border = BorderStroke(width = outlineWidth, color = outlineColor),
        shape = outlineShape,
    )
    val focusedBorder = Border(
        border = BorderStroke(width = focusedOutlineWidth, color = focusedOutlineColor),
        shape = outlineShape,
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
