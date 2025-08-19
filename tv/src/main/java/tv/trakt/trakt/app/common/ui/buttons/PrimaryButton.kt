package tv.trakt.trakt.app.common.ui.buttons

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.Text
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.common.ui.composables.FilmProgressIndicator
import tv.trakt.trakt.resources.R

@Composable
internal fun PrimaryButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    text: String,
    icon: Painter? = null,
    enabled: Boolean = true,
    loading: Boolean = false,
    containerColor: Color = TraktTheme.colors.primaryButtonContainer,
    contentColor: Color = TraktTheme.colors.primaryButtonContent,
    disabledContainerColor: Color = TraktTheme.colors.primaryButtonContainerDisabled,
    disabledContentColor: Color = TraktTheme.colors.primaryButtonContentDisabled,
    borderColor: Color = Color.White,
    disabledBorderColor: Color = Color.White,
) {
    val focusedBorder = Border(
        border = BorderStroke(
            width = (2.75).dp,
            color = if (enabled) borderColor else disabledBorderColor,
        ),
        shape = RoundedCornerShape(12.dp),
    )

    Button(
        contentPadding = PaddingValues(
            start = 12.dp,
            end = 12.dp,
        ),
        modifier = modifier
            .heightIn(max = 42.dp),
        shape = ButtonDefaults.shape(
            shape = RoundedCornerShape(12.dp),
            focusedDisabledShape = RoundedCornerShape(12.dp),
        ),
        border = ButtonDefaults.border(
            focusedBorder = focusedBorder,
            disabledBorder = Border.None,
            focusedDisabledBorder = focusedBorder,
        ),
        colors = ButtonDefaults.colors(
            containerColor = containerColor,
            contentColor = contentColor,
            focusedContainerColor = containerColor,
            focusedContentColor = contentColor,
            pressedContainerColor = containerColor,
            pressedContentColor = contentColor,
            disabledContainerColor = disabledContainerColor,
            disabledContentColor = disabledContentColor,
        ),
        scale = ButtonDefaults.scale(
            focusedScale = 1.04f,
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
                style = TraktTheme.typography.buttonPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = if (icon != null || loading) TextAlign.Start else TextAlign.Center,
                modifier = Modifier
                    .weight(1F),
            )

            when {
                loading -> {
                    FilmProgressIndicator(
                        size = 16.dp,
                        color = if (enabled) contentColor else disabledContentColor,
                    )
                }

                icon != null -> {
                    Image(
                        painter = icon,
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        colorFilter = ColorFilter.tint(if (enabled) contentColor else disabledContentColor),
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .requiredSize(20.dp),
                    )
                }
            }
        }
    }
}

@Preview(widthDp = 200)
@Composable
private fun Preview1() {
    TraktTheme {
        PrimaryButton(
            text = "Mark as something long",
        )
    }
}

@Preview(widthDp = 200)
@Composable
private fun Preview2() {
    TraktTheme {
        PrimaryButton(
            text = "Short",
            icon = painterResource(id = R.drawable.ic_check),
        )
    }
}

@Preview(widthDp = 200)
@Composable
private fun PreviewIcon() {
    TraktTheme {
        PrimaryButton(
            text = "Mark as something long",
            icon = painterResource(id = R.drawable.ic_check),
        )
    }
}

@Preview(widthDp = 200)
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
