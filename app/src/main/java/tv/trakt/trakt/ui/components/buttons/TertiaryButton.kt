package tv.trakt.trakt.ui.components.buttons

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun TertiaryButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    text: String,
    icon: Painter? = null,
    enabled: Boolean = true,
    loading: Boolean = false,
    height: Dp = 36.dp,
    containerColor: Color = TraktTheme.colors.primaryButtonContainer,
    contentColor: Color = TraktTheme.colors.primaryButtonContent,
    disabledContainerColor: Color = TraktTheme.colors.primaryButtonContainerDisabled,
    disabledContentColor: Color = TraktTheme.colors.primaryButtonContentDisabled,
) {
    PrimaryButton(
        modifier = modifier,
        onClick = onClick,
        text = text,
        icon = icon,
        iconSize = 16.dp,
        iconSpace = 8.dp,
        enabled = enabled,
        loading = loading,
        height = height,
        textStyle = TraktTheme.typography.buttonTertiary,
        containerColor = containerColor,
        contentColor = contentColor,
        disabledContainerColor = disabledContainerColor,
        disabledContentColor = disabledContentColor,
    )
}

@Preview()
@Composable
private fun Preview1() {
    TraktTheme {
        TertiaryButton(
            text = "Mark as something long",
        )
    }
}

@Preview
@Composable
private fun Preview2() {
    TraktTheme {
        TertiaryButton(
            text = "Short",
            icon = painterResource(id = R.drawable.ic_check),
        )
    }
}

@Preview()
@Composable
private fun PreviewIcon() {
    TraktTheme {
        TertiaryButton(
            text = "Mark as something long",
            icon = painterResource(id = R.drawable.ic_check),
        )
    }
}

@Preview
@Composable
private fun PreviewLoading() {
    TraktTheme {
        TertiaryButton(
            text = "Mark",
            enabled = false,
            loading = true,
        )
    }
}
