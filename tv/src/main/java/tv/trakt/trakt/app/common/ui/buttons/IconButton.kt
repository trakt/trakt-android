package tv.trakt.trakt.app.common.ui.buttons

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.IconButton
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.common.ui.composables.FilmProgressIndicator
import tv.trakt.trakt.resources.R

@Composable
internal fun IconButton(
    icon: Painter,
    modifier: Modifier = Modifier,
    iconSize: Dp = 24.dp,
    size: Dp = 56.dp,
    onClick: () -> Unit = {},
    enabled: Boolean = true,
    loading: Boolean = false,
    containerColor: Color = TraktTheme.colors.primaryButtonContainer,
    contentColor: Color = TraktTheme.colors.primaryButtonContent,
    disabledContainerColor: Color = TraktTheme.colors.primaryButtonContainerDisabled,
    disabledContentColor: Color = TraktTheme.colors.primaryButtonContentDisabled,
    borderColor: Color = Color.White,
    disabledBorderColor: Color = Color.White,
    focusedScale: Float = 1F,
) {
    val focusedBorder = Border(
        border = BorderStroke(
            width = (2.75).dp,
            color = if (enabled) borderColor else disabledBorderColor,
        ),
        shape = CircleShape,
    )

    IconButton(
        modifier = modifier
            .size(size),
        shape = ButtonDefaults.shape(
            shape = CircleShape,
            focusedDisabledShape = CircleShape,
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
            focusedScale = focusedScale,
        ),
        onClick = onClick,
        enabled = enabled,
    ) {
        if (loading) {
            FilmProgressIndicator(
                size = 18.dp,
                color = if (enabled) contentColor else disabledContentColor,
            )
        } else {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(size),
            ) {
                Image(
                    painter = icon,
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    colorFilter = ColorFilter.tint(if (enabled) contentColor else disabledContentColor),
                    modifier = Modifier.requiredSize(iconSize),
                )
            }
        }
    }
}

@Preview
@Composable
private fun Preview1() {
    TraktTheme {
        Column(
            verticalArrangement = spacedBy(12.dp),
        ) {
            IconButton(
                icon = painterResource(R.drawable.ic_subtitles),
            )
        }
    }
}
