import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Border
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import tv.trakt.trakt.app.ui.theme.TraktTheme

@Composable
internal fun AllStreamingItemView(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    name: String,
    country: String,
    logo: String? = null,
    price: String? = null,
    containerColor: Color = Color.White,
    contentColor: Color = Color.Black,
) {
    val focusedBorder = Border(
        border = BorderStroke(
            width = 3.dp,
            color = TraktTheme.colors.accent,
        ),
        shape = RoundedCornerShape(12.dp),
    )

    Button(
        contentPadding = PaddingValues(horizontal = 10.dp),
        modifier = modifier.height(42.dp),
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
            focusedScale = 1.05f,
        ),
        onClick = onClick,
    ) {
        Row(
            verticalAlignment = CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxSize(),
        ) {
            if (!logo.isNullOrBlank()) {
                AsyncImage(
                    model = "https://$logo",
                    contentDescription = "Logo",
                    contentScale = ContentScale.Fit,
                    colorFilter = ColorFilter.tint(contentColor),
                    modifier = Modifier.width(54.dp),
                )
            } else {
                Text(
                    text = name.uppercase(),
                    color = contentColor,
                    style = TraktTheme.typography.buttonPrimary.copy(fontSize = 15.sp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1F, fill = false)
                        .padding(end = 2.dp),
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

                Text(
                    text = country.uppercase(),
                    color = contentColor,
                    style = TraktTheme.typography.buttonTertiary,
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .border(2.dp, contentColor, RoundedCornerShape(4.dp))
                        .padding(horizontal = 5.dp, vertical = 3.dp),
                )
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
            country = "US",
            price = "$7.99",
            contentColor = Color.Blue,
        )
    }
}
