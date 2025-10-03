package tv.trakt.trakt.ui.components.buttons

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement.Start
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import tv.trakt.trakt.common.ui.composables.FilmProgressIndicator
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun WatchNowButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    text: String,
    textStyle: TextStyle = TraktTheme.typography.buttonPrimary,
    logo: String? = null,
    enabled: Boolean = true,
    loading: Boolean = false,
    height: Dp = 46.dp,
    corner: Dp = 14.dp,
    contentPadding: Dp = 12.dp,
    containerColor: Color = TraktTheme.colors.primaryButtonContainer,
    contentColor: Color = TraktTheme.colors.primaryButtonContent,
    disabledContainerColor: Color = TraktTheme.colors.primaryButtonContainerDisabled,
    disabledContentColor: Color = TraktTheme.colors.primaryButtonContentDisabled,
) {
    Button(
        contentPadding = PaddingValues(
            start = contentPadding,
            end = contentPadding,
        ),
        modifier = modifier.height(height),
        shape = RoundedCornerShape(corner),
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
            horizontalArrangement = Start,
        ) {
            when {
                loading -> {
                    FilmProgressIndicator(
                        size = 18.dp,
                        color = if (enabled) contentColor else disabledContentColor,
                        modifier = Modifier
                            .padding(end = 10.dp),
                    )
                }
                else ->
                    Image(
                        painter = painterResource(id = R.drawable.ic_play),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(if (enabled) contentColor else disabledContentColor),
                        modifier = Modifier
                            .padding(end = 10.dp)
                            .requiredSize(18.dp),
                    )
            }

            if (!logo.isNullOrBlank()) {
                AsyncImage(
                    model = "https://$logo",
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .padding(vertical = 2.dp)
                        .fillMaxWidth(),
                )
            } else {
                Text(
                    text = text.uppercase(),
                    color = if (enabled) contentColor else disabledContentColor,
                    style = textStyle,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Start,
                    autoSize = TextAutoSize.StepBased(
                        maxFontSize = textStyle.fontSize,
                        minFontSize = 10.sp,
                        stepSize = 2.sp,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Preview()
@Composable
private fun Preview1() {
    TraktTheme {
        WatchNowButton(
            text = "Netflix",
        )
    }
}

@Preview
@Composable
private fun Preview2() {
    TraktTheme {
        WatchNowButton(
            text = "Netflix",
            logo = "XXX",
        )
    }
}

@Preview()
@Composable
private fun PreviewIcon() {
    TraktTheme {
        WatchNowButton(
            text = "Mark as something long",
        )
    }
}

@Preview
@Composable
private fun PreviewLoading() {
    TraktTheme {
        WatchNowButton(
            text = "Mark",
            enabled = false,
            loading = true,
        )
    }
}
