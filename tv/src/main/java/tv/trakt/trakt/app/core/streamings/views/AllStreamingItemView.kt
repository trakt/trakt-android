package tv.trakt.trakt.app.core.streamings.views

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Arrangement.SpaceBetween
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.Text
import coil3.compose.AsyncImage
import com.murgupluoglu.flagkit.FlagKit
import tv.trakt.trakt.app.ui.theme.TraktTheme

@Composable
internal fun AllStreamingItemView(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    name: String,
    country: String,
    logo: String? = null,
    channel: String? = null,
    price: String? = null,
    containerColor: Color = Color.White,
    contentColor: Color = Color.Black,
) {
    val context = LocalContext.current

    val focusedBorder = Border(
        border = BorderStroke(
            width = 3.dp,
            color = TraktTheme.colors.accent,
        ),
        shape = RoundedCornerShape(12.dp),
    )

    Button(
        contentPadding = PaddingValues(horizontal = 10.dp),
        modifier = modifier.height(45.dp),
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
        ),
        scale = ButtonDefaults.scale(
            focusedScale = 1.04f,
        ),
        onClick = onClick,
    ) {
        Row(
            verticalAlignment = CenterVertically,
            horizontalArrangement = SpaceBetween,
            modifier = Modifier.fillMaxSize(),
        ) {
            if (!logo.isNullOrBlank()) {
                Row(
                    horizontalArrangement = spacedBy(2.dp),
                    verticalAlignment = CenterVertically,
                ) {
                    AsyncImage(
                        model = "https://$logo",
                        contentDescription = "Logo",
                        contentScale = ContentScale.Fit,
                        colorFilter = ColorFilter.tint(contentColor),
                        modifier = Modifier.width(54.dp),
                    )
                    if (!channel.isNullOrBlank()) {
                        AsyncImage(
                            model = "https://$channel",
                            contentDescription = "Channel Logo",
                            contentScale = ContentScale.Fit,
                            colorFilter = ColorFilter.tint(contentColor),
                            modifier = Modifier.width(42.dp),
                        )
                    }
                }
            } else {
                Text(
                    text = name.uppercase(),
                    color = contentColor,
                    style = TraktTheme.typography.buttonPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1F, fill = false)
                        .padding(end = 4.dp),
                )
            }

            Row(
                verticalAlignment = CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                if (!price.isNullOrBlank()) {
                    Text(
                        text = price.uppercase(),
                        color = contentColor,
                        style = TraktTheme.typography.buttonTertiary,
                        maxLines = 1,
                        textAlign = TextAlign.Center,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Row(
                    verticalAlignment = CenterVertically,
                    horizontalArrangement = spacedBy(2.dp),
                    modifier = Modifier
                        .border(1.2.dp, contentColor, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                ) {
                    val flagRes = remember {
                        val res = FlagKit.getResId(context, country)
                        if (res > 0) res else null
                    }
                    flagRes?.let {
                        Image(
                            painter = painterResource(flagRes),
                            contentDescription = "Flag",
                            modifier = Modifier
                                .height(12.dp)
                                .clip(RoundedCornerShape(2.dp)),
                        )
                    }
                    Text(
                        text = country.uppercase(),
                        color = contentColor,
                        style = TraktTheme.typography.buttonTertiary,
                        maxLines = 1,
                        textAlign = TextAlign.Center,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Preview(widthDp = 200)
@Composable
private fun Preview() {
    TraktTheme {
        AllStreamingItemView(
            name = "Amazon",
            country = "PL",
        )
    }
}

@Preview(widthDp = 200)
@Composable
private fun Preview2() {
    TraktTheme {
        AllStreamingItemView(
            name = "Netflix",
            country = "US",
            price = "$19.99",
        )
    }
}

@Preview(widthDp = 200)
@Composable
private fun Preview3() {
    TraktTheme {
        AllStreamingItemView(
            name = "Disney+",
            country = "ua",
            price = "$7.99",
            contentColor = Color.Blue,
        )
    }
}
