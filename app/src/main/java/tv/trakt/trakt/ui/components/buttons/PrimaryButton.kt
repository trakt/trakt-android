package tv.trakt.trakt.ui.components.buttons

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.R
import tv.trakt.trakt.ui.theme.TraktTheme

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
) {
    Button(
        contentPadding = PaddingValues(
            start = 12.dp,
            end = 12.dp,
        ),
        modifier = modifier
            .heightIn(max = 42.dp),
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
                style = TraktTheme.typography.buttonPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = if (icon != null || loading) TextAlign.Start else TextAlign.Center,
                modifier = Modifier
                    .weight(1F),
            )

            when {
                loading -> {
//                    FilmProgressIndicator(
//                        size = 16.dp,
//                        color = if (enabled) contentColor else disabledContentColor,
//                    )
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
