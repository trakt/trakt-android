package tv.trakt.trakt.app.common.ui.buttons

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
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
internal fun LikeButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    text: String,
    liked: Boolean = false,
    enabled: Boolean = true,
    loading: Boolean = false,
    containerColor: Color = TraktTheme.colors.primaryButtonContainer,
    contentColor: Color = TraktTheme.colors.primaryButtonContent,
    disabledContainerColor: Color = TraktTheme.colors.primaryButtonContainerDisabled,
    disabledContentColor: Color = TraktTheme.colors.primaryButtonContentDisabled,
    borderColor: Color = Color.White,
    disabledBorderColor: Color = Color.White,
    focusedScale: Float = 1.04F,
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
            .wrapContentWidth()
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
            focusedScale = focusedScale,
        ),
        onClick = onClick,
        enabled = enabled,
    ) {
        if (loading) {
            FilmProgressIndicator(
                size = 18.dp,
                color = if (enabled) contentColor else disabledContentColor,
                modifier = Modifier.widthIn(min = 64.dp),
            )
        } else {
            Row(
                verticalAlignment = CenterVertically,
                horizontalArrangement = spacedBy(8.dp, Alignment.CenterHorizontally),
                modifier = Modifier.widthIn(min = 64.dp),
            ) {
                Image(
                    painter = painterResource(
                        when {
                            liked -> R.drawable.ic_thumb_up2_fill
                            else -> R.drawable.ic_thumb_up2
                        },
                    ),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    colorFilter = ColorFilter.tint(if (enabled) contentColor else disabledContentColor),
                    modifier = Modifier
                        .requiredSize(20.dp),
                )

                Text(
                    text = text.uppercase(),
                    color = if (enabled) contentColor else disabledContentColor,
                    style = TraktTheme.typography.buttonPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1F, false),
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
            LikeButton(
                text = "12",
            )
            LikeButton(
                text = "12",
                loading = true,
                enabled = false,
            )
        }
    }
}
