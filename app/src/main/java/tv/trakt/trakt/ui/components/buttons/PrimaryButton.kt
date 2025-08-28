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
internal fun PrimaryButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    text: String,
    textStyle: TextStyle = TraktTheme.typography.buttonPrimary,
    icon: Painter? = null,
    iconSize: Dp = 18.dp,
    iconSpace: Dp = 8.dp,
    enabled: Boolean = true,
    loading: Boolean = false,
    height: Dp = 40.dp,
    containerColor: Color = TraktTheme.colors.primaryButtonContainer,
    contentColor: Color = TraktTheme.colors.primaryButtonContent,
    disabledContainerColor: Color = TraktTheme.colors.primaryButtonContainerDisabled,
    disabledContentColor: Color = TraktTheme.colors.primaryButtonContentDisabled,
) {
    Button(
        contentPadding = PaddingValues(
            start = 12.dp,
            end = 12.dp,
        ),
        modifier = modifier.height(height),
        shape = RoundedCornerShape(12.dp),
        colors = buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = disabledContainerColor,
            disabledContentColor = disabledContentColor,
        ),
        onClick = onClick,
        enabled = enabled,
    ) {
        Row(
            verticalAlignment = CenterVertically,
        ) {
            Text(
                text = text.uppercase(),
                color = if (enabled) contentColor else disabledContentColor,
                style = textStyle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = if (icon != null || loading) TextAlign.Start else TextAlign.Center,
                modifier = Modifier,
            )

            when {
                loading -> {
                    FilmProgressIndicator(
                        size = iconSize - 4.dp,
                        color = if (enabled) contentColor else disabledContentColor,
                        modifier = Modifier
                            .padding(start = 8.dp),
                    )
                }

                icon != null -> {
                    Image(
                        painter = icon,
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        colorFilter = ColorFilter.tint(if (enabled) contentColor else disabledContentColor),
                        modifier = Modifier
                            .padding(start = iconSpace)
                            .requiredSize(iconSize),
                    )
                }
            }
        }
    }
}

@Preview()
@Composable
private fun Preview1() {
    TraktTheme {
        PrimaryButton(
            text = "Mark as something long",
        )
    }
}

@Preview
@Composable
private fun Preview2() {
    TraktTheme {
        PrimaryButton(
            text = "Short",
            icon = painterResource(id = R.drawable.ic_check),
        )
    }
}

@Preview()
@Composable
private fun PreviewIcon() {
    TraktTheme {
        PrimaryButton(
            text = "Mark as something long",
            icon = painterResource(id = R.drawable.ic_check),
        )
    }
}

@Preview
@Composable
private fun PreviewLoading() {
    TraktTheme {
        PrimaryButton(
            text = "Mark",
            enabled = false,
            loading = true,
        )
    }
}
