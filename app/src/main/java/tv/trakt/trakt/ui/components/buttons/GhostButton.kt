package tv.trakt.trakt.ui.components.buttons

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.ui.composables.FilmProgressIndicator
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun GhostButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    text: String,
    textStyle: TextStyle = TraktTheme.typography.buttonSecondary,
    icon: Painter? = null,
    iconSize: Dp = 22.dp,
    iconSpace: Dp = 10.dp,
    enabled: Boolean = true,
    loading: Boolean = false,
    corner: Dp = 8.dp,
    containerColor: Color = Color.Transparent,
    contentColor: Color = TraktTheme.colors.primaryButtonContent,
    disabledContainerColor: Color = Color.Transparent,
    disabledContentColor: Color = TraktTheme.colors.primaryButtonContent.copy(alpha = 0.25F),
) {
    Button(
        modifier = modifier
            .height(iconSize + 8.dp),
        enabled = enabled,
        contentPadding = PaddingValues(
            horizontal = 4.dp,
            vertical = 0.dp,
        ),
        shape = RoundedCornerShape(corner),
        colors = buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = disabledContainerColor,
            disabledContentColor = disabledContentColor,
        ),
        interactionSource = null,
        onClick = onClick,
    ) {
        Row(
            verticalAlignment = CenterVertically,
        ) {
            when {
                loading -> {
                    FilmProgressIndicator(
                        size = iconSize,
                        color = if (enabled) contentColor else disabledContentColor,
                        modifier = Modifier
                            .padding(end = iconSpace),
                    )
                }

                icon != null -> {
                    Image(
                        painter = icon,
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        colorFilter = ColorFilter.tint(if (enabled) contentColor else disabledContentColor),
                        modifier = Modifier
                            .padding(end = iconSpace)
                            .requiredSize(iconSize),
                    )
                }
            }

            Text(
                text = text.uppercase(),
                color = if (enabled) contentColor else disabledContentColor,
                style = textStyle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = if (icon != null || loading) TextAlign.Start else TextAlign.Center,
                modifier = Modifier,
            )
        }
    }
}

@Preview()
@Composable
private fun Preview1() {
    TraktTheme {
        GhostButton(
            text = "Mark as something long",
        )
    }
}

@Preview
@Composable
private fun Preview2() {
    TraktTheme {
        GhostButton(
            text = "Short",
            icon = painterResource(id = R.drawable.ic_check),
        )
    }
}

@Preview()
@Composable
private fun PreviewIcon() {
    TraktTheme {
        GhostButton(
            text = "Mark as something long",
            icon = painterResource(id = R.drawable.ic_check),
        )
    }
}

@Preview
@Composable
private fun PreviewLoading() {
    TraktTheme {
        GhostButton(
            text = "Mark",
            enabled = false,
            loading = true,
        )
    }
}
