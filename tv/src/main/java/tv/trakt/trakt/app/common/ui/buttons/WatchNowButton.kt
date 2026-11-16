package tv.trakt.trakt.app.common.ui.buttons

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Border
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.Text
import coil3.compose.AsyncImage
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.common.ui.composables.FilmProgressIndicator
import tv.trakt.trakt.resources.R

// ic_play_round has wide built-in padding and its triangle sits right of centre,
// so it is drawn larger than the trailing slot and shifted back onto the slot centre.
private val playIconSize = 30.dp
private val playIconOffset = (-1.3).dp

@Composable
internal fun WatchNowButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
    name: String,
    logo: String? = null,
    text: String = stringResource(R.string.button_text_stream),
    secondaryText: String? = null,
    enabled: Boolean = true,
    loading: Boolean = false,
    containerColor: Color = TraktTheme.colors.primaryButtonContainer,
    contentColor: Color = TraktTheme.colors.primaryButtonContent,
    disabledContainerColor: Color = TraktTheme.colors.primaryButtonContainerDisabled,
    disabledContentColor: Color = TraktTheme.colors.primaryButtonContentDisabled,
) {
    val focusedBorder = Border(
        border = BorderStroke(width = (2.75).dp, color = Color.White),
        shape = RoundedCornerShape(12.dp),
    )

    Button(
        contentPadding = PaddingValues(horizontal = buttonContentPadding),
        modifier = modifier.height(buttonHeight),
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
        onLongClick = onLongClick,
        enabled = enabled,
    ) {
        Row(
            verticalAlignment = CenterVertically,
            modifier = Modifier.fillMaxHeight(),
        ) {
            Column {
                Text(
                    text = text.uppercase(),
                    color = contentColor,
                    style = TraktTheme.typography.buttonPrimary,
                )
                if (!secondaryText.isNullOrBlank()) {
                    Text(
                        text = secondaryText.uppercase().take(24),
                        color = contentColor.copy(alpha = 0.66f),
                        style = TraktTheme.typography.meta.copy(fontSize = 9.sp),
                    )
                }
            }

            when {
                loading -> {
                    Spacer(modifier = Modifier.weight(1F))
                    ButtonTrailingSlot {
                        FilmProgressIndicator(
                            size = 16.dp,
                            color = contentColor,
                        )
                    }
                }

                else -> {
                    if (!logo.isNullOrBlank()) {
                        Spacer(modifier = Modifier.weight(1F))
                        AsyncImage(
                            model = "https://$logo",
                            contentDescription = "Logo",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .width(48.dp),
                        )
                    } else {
                        Text(
                            text = name.uppercase(),
                            color = contentColor,
                            style = TraktTheme.typography.buttonPrimary,
                            textAlign = TextAlign.End,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .weight(1f, fill = true),
                        )
                    }

                    ButtonTrailingSlot {
                        Image(
                            painter = painterResource(R.drawable.ic_play_round),
                            contentDescription = null,
                            contentScale = ContentScale.FillWidth,
                            modifier = Modifier
                                .requiredSize(playIconSize)
                                .offset(x = playIconOffset),
                        )
                    }
                }
            }
        }
    }
}

@Preview(widthDp = 200)
@Composable
private fun Preview() {
    TraktTheme {
        WatchNowButton(
            name = "Netflix",
        )
    }
}

@Preview(widthDp = 200)
@Composable
private fun Preview2() {
    TraktTheme {
        WatchNowButton(
            name = "Netflix",
            logo = "XXX",
        )
    }
}

@Preview(widthDp = 200)
@Composable
private fun Preview3() {
    TraktTheme {
        WatchNowButton(
            name = "Netflix",
            secondaryText = "Long press for more",
        )
    }
}

@Preview(widthDp = 200)
@Composable
private fun PreviewDisabled() {
    TraktTheme {
        WatchNowButton(
            name = "CDA LoremIpsum posiadk",
            enabled = false,
        )
    }
}

@Preview(widthDp = 200)
@Composable
private fun PreviewLoading() {
    TraktTheme {
        WatchNowButton(
            name = "Netflix",
            enabled = false,
            loading = true,
        )
    }
}
